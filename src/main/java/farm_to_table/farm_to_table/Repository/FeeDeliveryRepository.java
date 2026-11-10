package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.FeeDeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeDeliveryRepository extends JpaRepository<FeeDeliveryEntity, Long> {
    @Query("""
            SELECT f FROM FeeDeliveryEntity f
            WHERE (:keyword = '' OR LOWER(f.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<FeeDeliveryEntity> search(@Param("keyword") String keyword, Pageable pageable);
}

