package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.FeeDelivery.FeeDeliveryRequestDto;
import farm_to_table.farm_to_table.DTO.FeeDelivery.FeeDeliveryResponseDto;
import farm_to_table.farm_to_table.Repository.FeeDeliveryRepository;
import farm_to_table.farm_to_table.model.FeeDeliveryEntity;
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
public class FeeDeliveryServiceImpl implements FeeDeliveryService {
    private final FeeDeliveryRepository repo;

    @Override
    public FeeDeliveryResponseDto create(FeeDeliveryRequestDto r) {
        FeeDeliveryEntity entity = new FeeDeliveryEntity();
        entity.setCode(r.getCode());
        entity.setAmount(r.getAmount());
        entity.setDescription(r.getDescription());
        return toDto(repo.save(entity));
    }

    @Override
    public FeeDeliveryResponseDto update(Long id, FeeDeliveryRequestDto r) {
        FeeDeliveryEntity entity = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery fee with id " + id + " not found"));
        entity.setCode(r.getCode());
        entity.setAmount(r.getAmount());
        entity.setDescription(r.getDescription());
        return toDto(repo.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery fee with id " + id + " not found");
        }
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeDeliveryResponseDto getById(Long id) {
        return repo.findById(id).map(this::toDto).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery fee with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeDeliveryResponseDto> getAll(String keyword, int page, int size) {
        String k = keyword == null ? "" : keyword.trim();
        PageRequest pageRequest = PageRequest.of(page, Math.max(size, 1), Sort.by("id").ascending());
        return repo.search(k, pageRequest).map(this::toDto).getContent();
    }

    private FeeDeliveryResponseDto toDto(FeeDeliveryEntity e) {
        FeeDeliveryResponseDto dto = new FeeDeliveryResponseDto();
        dto.setId(e.getId());
        dto.setCode(e.getCode());
        dto.setAmount(e.getAmount());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt() : LocalDateTime.now());
        dto.setUpdatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt() : LocalDateTime.now());
        return dto;
    }
}
