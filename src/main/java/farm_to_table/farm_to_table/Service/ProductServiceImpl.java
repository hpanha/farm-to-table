package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Product.ProductRequest;
import farm_to_table.farm_to_table.DTO.Product.ProductResponse;
import farm_to_table.farm_to_table.Repository.ProductImageRepository;
import farm_to_table.farm_to_table.Repository.ProductRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
import farm_to_table.farm_to_table.model.ProductEntity;
import farm_to_table.farm_to_table.model.ProductImageEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    static final int MAX_IMAGES_PER_PRODUCT = 5;

    static final String APPROVED = "APPROVED";
    static final String PENDING = "PENDING";
    static final String REJECTED = "REJECTED";

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final UserRepository userRepository;

    /**
     * Dev/test only: lets a farmer approve their own product (the "bypass approve" button).
     * Off by default; production approval is done by an ADMIN.
     */
    @Value("${app.dev.bypass-approval:false}")
    private boolean bypassApproval;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest r) {
        long imageCount = r.getImages() == null ? 0
                : r.getImages().stream().filter(s -> s != null && !s.isBlank()).count();
        if (imageCount > MAX_IMAGES_PER_PRODUCT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A product can have at most " + MAX_IMAGES_PER_PRODUCT + " images");
        }
        ProductEntity entity = new ProductEntity();
        copyProperties(r, entity);
        // New listings wait for approval; customers do not see them until approved.
        entity.setApprovalStatus(PENDING);
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
    @Transactional
    public ProductResponse setApproval(Long id, String status, String actorUsername) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));
        Viewer actor = viewer(actorUsername);
        if (actor.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Log in to approve products");
        }
        boolean isOwner = actor.userId().equals(entity.getUserId());
        if (!actor.admin()) {
            // Owners may only bypass-approve their own product, and only when the dev flag is on.
            if (!(bypassApproval && isOwner && APPROVED.equals(status))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only an ADMIN can change product approval");
            }
        }
        entity.setApprovalStatus(status);
        return toResponse(productRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id, String viewerUsername) {
        Viewer viewer = viewer(viewerUsername);
        return productRepository.findById(id)
                .filter(p -> isVisible(p, viewer))
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(String viewerUsername) {
        Viewer viewer = viewer(viewerUsername);
        return productRepository.findAll().stream()
                .filter(p -> isVisible(p, viewer))
                .map(this::toResponse)
                .toList();
    }

    // ---------- visibility ----------

    private record Viewer(String userId, boolean admin) {}

    private Viewer viewer(String username) {
        if (username == null) return new Viewer(null, false);
        return userRepository.findByUsername(username)
                .map(u -> new Viewer(String.valueOf(u.getId()),
                        u.getRolesId() != null && u.getRolesId() == 1L))
                .orElse(new Viewer(null, false));
    }

    /**
     * Admin sees all; the owner sees their own products (any status).
     * Everyone else sees a product only once the product itself is APPROVED
     * (null = legacy product, treated as approved). The product approval is the only gate.
     */
    private boolean isVisible(ProductEntity p, Viewer viewer) {
        if (viewer.admin()) return true;
        if (viewer.userId() != null && viewer.userId().equals(p.getUserId())) return true;
        return p.getApprovalStatus() == null || APPROVED.equals(p.getApprovalStatus());
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
                .approvalStatus(e.getApprovalStatus() == null ? APPROVED : e.getApprovalStatus())
                .build();
    }
}
