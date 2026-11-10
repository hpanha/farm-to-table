package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.CashoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashoutRepository extends JpaRepository<CashoutEntity, Long> {
    List<CashoutEntity> findByFarmerUserIdOrderByIdDesc(String farmerUserId);
    List<CashoutEntity> findAllByOrderByIdDesc();
    List<CashoutEntity> findByStatusOrderByIdDesc(String status);
}
