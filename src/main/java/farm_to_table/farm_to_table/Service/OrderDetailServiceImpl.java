package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.OrderDetail.OrderDetailRequestDto;
import farm_to_table.farm_to_table.DTO.OrderDetail.OrderDetailResponseDto;
import farm_to_table.farm_to_table.Repository.OrderDetailRepository;
import farm_to_table.farm_to_table.model.OrderDetailEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository repository;

    @Override
    public OrderDetailResponseDto create(OrderDetailRequestDto request) {
        OrderDetailEntity entity = new OrderDetailEntity();
        copyFromRequest(request, entity);
        return toDto(repository.save(entity));
    }

    @Override
    public OrderDetailResponseDto update(Long id, OrderDetailRequestDto request) {
        OrderDetailEntity entity = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order detail with id " + id + " not found"));
        copyFromRequest(request, entity);
        return toDto(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Order detail with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponseDto getById(Long id) {
        return repository.findById(id).map(this::toDto).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order detail with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailResponseDto> getAll(String orderId, int page, int size) {
        if (orderId != null && !orderId.trim().isEmpty()) {
            PageRequest pageRequest = PageRequest.of(page, Math.max(size, 1), Sort.by("id").ascending());
            return repository.findByOrderId(orderId.trim(), pageRequest).map(this::toDto).getContent();
        }
        PageRequest pageRequest = PageRequest.of(page, Math.max(size, 1), Sort.by("id").ascending());
        return repository.findAll(pageRequest).map(this::toDto).getContent();
    }

    private void copyFromRequest(OrderDetailRequestDto req, OrderDetailEntity entity) {
        entity.setOrderId(req.getOrderId());
        entity.setProductId(req.getProductId());
        entity.setQty(req.getQty());
        entity.setUnitPrice(req.getUnitPrice());
        entity.setSubTotal(req.getQty() != null && req.getUnitPrice() != null ? req.getQty() * req.getUnitPrice() : 0.0);
        entity.setOtherMethodId(req.getOtherMethodId());
        entity.setPaymentMethodId(req.getPaymentMethodId());
    }

    private OrderDetailResponseDto toDto(OrderDetailEntity e) {
        OrderDetailResponseDto dto = new OrderDetailResponseDto();
        dto.setId(e.getId());
        dto.setOrderId(e.getOrderId());
        dto.setProductId(e.getProductId());
        dto.setQty(e.getQty());
        dto.setUnitPrice(e.getUnitPrice());
        dto.setSubTotal(e.getSubTotal());
        dto.setOtherMethodId(e.getOtherMethodId());
        dto.setPaymentMethodId(e.getPaymentMethodId());
        dto.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt() : LocalDateTime.now());
        dto.setUpdatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt() : LocalDateTime.now());
        return dto;
    }
}
