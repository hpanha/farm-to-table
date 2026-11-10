package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Category.CategoryRequest;
import farm_to_table.farm_to_table.DTO.Category.CategoryResponse;
import farm_to_table.farm_to_table.Repository.CategoryRepository;
import farm_to_table.farm_to_table.model.CategoryEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest r) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(r.getName());
        entity.setDescription(r.getDescription());
        entity.setStatus(r.getStatus() != null ? r.getStatus() : "ACTIVE");
        entity.setImage(r.getImage());
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest r) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));
        entity.setName(r.getName());
        entity.setDescription(r.getDescription());
        entity.setStatus(r.getStatus());
        if (r.getImage() != null) entity.setImage(r.getImage()); // keep the old picture when none is sent
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CategoryResponse toResponse(CategoryEntity e) {
        return CategoryResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .status(e.getStatus())
                .image(e.getImage())
                .build();
    }
}
