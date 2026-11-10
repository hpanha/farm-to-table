package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.OrderDetailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    List<OrderDetailEntity> findByOrderId(String orderId);
    Page<OrderDetailEntity> findByOrderId(String orderId, Pageable pageable);
}
