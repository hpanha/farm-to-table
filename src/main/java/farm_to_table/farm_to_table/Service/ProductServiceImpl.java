package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Product.ProductRequest;
import farm_to_table.farm_to_table.DTO.Product.ProductResponse;
import farm_to_table.farm_to_table.Repository.ProductImageRepository;
import farm_to_table.farm_to_table.Repository.ProductRepository;
import farm_to_table.farm_to_table.model.ProductEntity;
import farm_to_table.farm_to_table.model.ProductImageEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest r) {
        ProductEntity entity = new ProductEntity();
        copyProperties(r, entity);
        ProductEntity saved = productRepository.save(entity);
        if (r.getImages() != null && !r.getImages().isEmpty()) {
            for (String imgPath : r.getImages()) {
                if (imgPath != null && !imgPath.isBlank()) {
                    ProductImageEntity pie = new ProductImageEntity();
                    pie.setProduct(saved);
                    pie.setPath(imgPath.trim());
                    imageRepository.save(pie);
                }
            }
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest r) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        copyProperties(r, entity);
        return toResponse(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void copyProperties(ProductRequest r, ProductEntity e) {
        e.setUserId(r.getUserId());
        e.setCategoryId(r.getCategoryId());
        e.setName(r.getName());
        e.setQty(r.getQty());
        e.setUnitPrice(r.getUnitPrice());
        e.setFarmerPrice(r.getFarmerPrice());
        e.setExpectedHavestDate(r.getExpectedHavestDate());
        e.setExpiredDate(r.getExpiredDate());
        e.setLocation(r.getLocation());
    }

    private ProductResponse toResponse(ProductEntity e) {
        List<String> images = Collections.emptyList();
        try {
            images = imageRepository.findByProduct_Id(e.getId())
                    .stream()
                    .map(ProductImageEntity::getPath)
                    .toList();
        } catch (Exception ignored) {
        }

        return ProductResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .categoryId(e.getCategoryId())
                .name(e.getName())
                .qty(e.getQty())
                .unitPrice(e.getUnitPrice())
                .farmerPrice(e.getFarmerPrice())
                .expectedHavestDate(e.getExpectedHavestDate())
                .expiredDate(e.getExpiredDate())
                .location(e.getLocation())
                .images(images)
                .build();
    }
}
