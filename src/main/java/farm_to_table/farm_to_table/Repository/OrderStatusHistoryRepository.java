package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.OrderStatusHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    Page<OrderStatusHistoryEntity> findByOrderId(String orderId, Pageable pageable);
}

