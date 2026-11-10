package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Order.OrderRequestDto;
import farm_to_table.farm_to_table.DTO.Order.OrderResponseDto;
import farm_to_table.farm_to_table.DTO.Order.OrderStatusDto;
import farm_to_table.farm_to_table.DTO.Order.OrderUpdateDto;
import farm_to_table.farm_to_table.Repository.CustomerRepository;
import farm_to_table.farm_to_table.Repository.OrderRepository;
import farm_to_table.farm_to_table.Repository.StatusRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
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