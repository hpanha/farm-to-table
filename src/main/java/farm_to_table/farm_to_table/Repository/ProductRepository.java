package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByUserId(String userId);
    List<ProductEntity> findByCategoryId(String categoryId);
}

