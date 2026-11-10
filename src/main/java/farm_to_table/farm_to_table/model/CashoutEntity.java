package farm_to_table.farm_to_table.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** A farmer's request to collect earnings in cash at a warehouse. */
@Entity
@Table(name = "cashout_request")
@Getter
@Setter
public class CashoutEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String farmerUserId;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseAddress;
    private Double amount;

    /** REQUESTED (waiting at the warehouse) or PAID (cash handed over). */
    private String status;
    private String reference;
    private LocalDateTime paidAt;
}
