package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);
}

