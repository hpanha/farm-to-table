package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Order.OrderRequestDto;
import farm_to_table.farm_to_table.DTO.Order.OrderResponseDto;
import farm_to_table.farm_to_table.DTO.Order.OrderStatusDto;
import farm_to_table.farm_to_table.DTO.Order.OrderUpdateDto;
import farm_to_table.farm_to_table.Repository.CustomerRepository;
import farm_to_table.farm_to_table.Repository.ProductRepository;
import farm_to_table.farm_to_table.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import farm_to_table.farm_to_table.Repository.OrderDetailRepository;
import farm_to_table.farm_to_table.Repository.OrderRepository;
import farm_to_table.farm_to_table.Repository.StatusRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
import farm_to_table.farm_to_table.model.OrderDetailEntity;
import farm_to_table.farm_to_table.model.OrderEntity;
import farm_to_table.farm_to_table.model.OrderStatus;
import farm_to_table.farm_to_table.model.StatusEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_TYPE = "ORDER";

    private final OrderRepository repository;
    private final CustomerRepository customerRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CommissionService commissionService;
    private final ProductRepository productRepository;

    /** Dev/test only (same flag as the product bypass). */
    @Value("${app.dev.bypass-approval:false}")
    private boolean devBypass;

    @Override
    @Transactional
    public OrderResponseDto devDeliver(Long id, String actorUsername) {
        if (!devBypass) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dev bypass is disabled on the server");
        }
        OrderEntity entity = findOrThrow(id);
        UserEntity actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in first"));
        boolean admin = actor.getRolesId() != null && actor.getRolesId() == 1L;
        if (!admin && !ownsALineIn(entity, String.valueOf(actor.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only an admin or the farmer of a product in this order can mark it delivered");
        }

        // Walk the normal steps: PENDING -> CONFIRMED -> SHIPPING -> DELIVERED.
        while (currentStatus(entity) != OrderStatus.DELIVERED) {
            OrderStatus current = currentStatus(entity);
            if (current == OrderStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A cancelled order cannot be delivered");
            }
            OrderStatus next = nextStep(current);
            if (next == OrderStatus.DELIVERED) {
                snapshotCommission(entity);
            }
            applyStatus(entity, resolveStatus(next));
        }
        return toDto(repository.save(entity));
    }

    private static OrderStatus nextStep(OrderStatus current) {
        return switch (current) {
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.SHIPPING;
            case SHIPPING -> OrderStatus.DELIVERED;
            default -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot advance from " + current);
        };
    }

    private boolean ownsALineIn(OrderEntity order, String farmerUserId) {
        return orderDetailRepository.findByOrderId(String.valueOf(order.getId())).stream()
                .anyMatch(line -> {
                    try {
                        return productRepository.findById(Long.parseLong(line.getProductId().trim()))
                                .map(p -> farmerUserId.equals(p.getUserId()))
                                .orElse(false);
                    } catch (RuntimeException e) {
                        return false;
                    }
                });
    }

    @Override
    public OrderResponseDto create(OrderRequestDto dto) {
        requireCustomerExists(dto.getCustomerId());

        StatusEntity pending = resolveStatus(OrderStatus.PENDING);

        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(dto.getCustomerId());
        entity.setOrderNumber(generateOrderNumber());
        applyStatus(entity, pending);
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        return toDto(repository.save(entity));
    }

    @Override
    public OrderResponseDto update(Long id, OrderUpdateDto dto) {
        OrderEntity entity = findOrThrow(id);

        if (currentStatus(entity) != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only PENDING orders can be edited (current: " + entity.getOrderStatus() + ")");
        }

        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setTotalAmount(dto.getTotalAmount());
        return toDto(repository.save(entity));
    }

    @Override
    public OrderResponseDto updateStatus(Long id, OrderStatusDto dto) {
        OrderEntity entity = findOrThrow(id);

        OrderStatus next;
        try {
            next = OrderStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed: PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED");
        }

        OrderStatus current = currentStatus(entity);
        if (!current.canChangeTo(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change order status from " + current + " to " + next
                            + ". Allowed: " + current.allowedNext());
        }

        if (next == OrderStatus.DELIVERED) {
            snapshotCommission(entity);
        }
        applyStatus(entity, resolveStatus(next));
        return toDto(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        OrderEntity entity = findOrThrow(id);
        OrderStatus current = currentStatus(entity);

        if (current != OrderStatus.PENDING && current != OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only PENDING or CANCELLED orders can be deleted (current: " + current + ")");
        }
        repository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order " + orderNumber + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> search(String customerId, String status,
                                         String keyword, Pageable pageable) {
        String c = customerId == null ? "" : customerId.trim();
        String s = status == null ? "" : status.trim().toUpperCase();
        String k = keyword == null ? "" : keyword.trim();
        return repository.search(c, s, k, pageable).map(this::toDto);
    }

    // ---------- helpers ----------

    private OrderEntity findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order with id " + id + " not found"));
    }

    private OrderStatus currentStatus(OrderEntity entity) {
        return OrderStatus.valueOf(entity.getOrderStatus());
    }

    // ទាញ status ពីតារាង status_entity (type = ORDER)
    private StatusEntity resolveStatus(OrderStatus status) {
        return statusRepository.findByCodeAndType(status.name(), ORDER_TYPE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Status " + status + " (type " + ORDER_TYPE
                                + ") is missing in the status table"));
    }

    // កំណត់ទាំង orderStatus (code) និង statusId ឱ្យត្រូវគ្នាជានិច្ច
    private void applyStatus(OrderEntity entity, StatusEntity status) {
        entity.setOrderStatus(status.getCode());
        entity.setStatusId(String.valueOf(status.getId()));
    }

    /**
     * On delivery, freeze the commission for every line: gross = sub_total (product price only,
     * delivery fee is excluded), commission = gross x rate, farmer net = gross - commission.
     */
    private void snapshotCommission(OrderEntity order) {
        double rate = commissionService.getRate().doubleValue();
        for (OrderDetailEntity line : orderDetailRepository.findByOrderId(String.valueOf(order.getId()))) {
            double gross = line.getSubTotal() == null ? 0.0 : line.getSubTotal();
            double commission = CommissionService.round2(gross * rate);
            line.setCommissionRate(rate);
            line.setCommissionAmount(commission);
            line.setFarmerNetAmount(CommissionService.round2(gross - commission));
            orderDetailRepository.save(line);
        }
    }

    private void requireCustomerExists(String customerId) {
        long id;
        try {
            id = Long.parseLong(customerId.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId must be a number");
        }
        if (!customerRepository.existsById(id) && !userRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer/User with id " + customerId + " not found");
        }
    }

    // ឧទាហរណ៍: ORD-20261029-483921
    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String number;
        do {
            number = "ORD-" + date + "-" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (repository.existsByOrderNumber(number));
        return number;
    }

    private OrderResponseDto toDto(OrderEntity e) {
        return new OrderResponseDto(
                e.getId(), e.getCustomerId(), e.getOrderNumber(),
                e.getOrderStatus(), e.getTotalAmount(), e.getDeliveryAddress(),
                e.getStatusId());
    }
}