package farm_to_table.farm_to_table.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderDetailEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    private String productId;
    private Integer qty;
    private Double unitPrice;
    private Double subTotal;
    private String otherMethodId;
    private String paymentMethodId;

    // Commission snapshot, filled once when the order is DELIVERED (null before that).
    private Double commissionRate;
    private Double commissionAmount;
    private Double farmerNetAmount;

    /** Set when the farmer requests a cash-out that includes this line (prevents double cash-out). */
    private Long cashoutId;
}
