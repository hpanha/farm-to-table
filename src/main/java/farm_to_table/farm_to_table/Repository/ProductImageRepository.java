package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.ProductImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    boolean existsByProduct_IdAndPath(Long productId, String path);

    boolean existsByProduct_IdAndPathAndIdNot(Long productId, String path, Long id);

    Optional<ProductImageEntity> findByIdAndProduct_Id(Long id, Long productId);

    java.util.List<ProductImageEntity> findByProduct_Id(Long productId);

    Page<ProductImageEntity> findByProduct_IdAndPathContainingIgnoreCase(
            Long productId, String path, Pageable pageable);
}