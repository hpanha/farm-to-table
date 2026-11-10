package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.OrderStatusHistory.OrderStatusHistoryRequestDto;
import farm_to_table.farm_to_table.DTO.OrderStatusHistory.OrderStatusHistoryResponseDto;
import farm_to_table.farm_to_table.Repository.OrderStatusHistoryRepository;
import farm_to_table.farm_to_table.model.OrderStatusHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository repository;

    @Override
    public OrderStatusHistoryResponseDto create(OrderStatusHistoryRequestDto request) {
        OrderStatusHistoryEntity entity = new OrderStatusHistoryEntity();
        copyFromRequest(request, entity);
        return toDto(repository.save(entity));
    }

    @Override
    public OrderStatusHistoryResponseDto update(Long id, OrderStatusHistoryRequestDto request) {
        OrderStatusHistoryEntity entity = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order status history with id " + id + " not found"));
        copyFromRequest(request, entity);
        return toDto(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Order status history with id " + id + " not found");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusHistoryResponseDto getById(Long id) {
        return repository.findById(id).map(this::toDto).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order status history with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponseDto> getAll(String orderId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.max(size, 1), Sort.by("id").descending());
        if (orderId != null && !orderId.trim().isEmpty()) {
            return repository.findByOrderId(orderId.trim(), pageRequest).map(this::toDto).getContent();
        }
        return repository.findAll(pageRequest).map(this::toDto).getContent();
    }

    private void copyFromRequest(OrderStatusHistoryRequestDto req, OrderStatusHistoryEntity entity) {
        entity.setOrderId(req.getOrderId());
        entity.setOrderStatusId(req.getOrderStatusId());
        entity.setNewStatusId(req.getNewStatusId());
        entity.setRemark(req.getRemark());
        entity.setChangeAt(req.getChangeAt() != null
                ? Date.from(req.getChangeAt().atZone(ZoneId.systemDefault()).toInstant())
                : new Date());
    }

    private OrderStatusHistoryResponseDto toDto(OrderStatusHistoryEntity e) {
        OrderStatusHistoryResponseDto dto = new OrderStatusHistoryResponseDto();
        dto.setId(e.getId());
        dto.setOrderId(e.getOrderId());
        dto.setOrderStatusId(e.getOrderStatusId());
        dto.setNewStatusId(e.getNewStatusId());
        dto.setRemark(e.getRemark());
        if (e.getChangeAt() != null) {
            dto.setChangeAt(e.getChangeAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        dto.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt() : LocalDateTime.now());
        dto.setUpdatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt() : LocalDateTime.now());
        return dto;
    }
}
