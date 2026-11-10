package farm_to_table.farm_to_table.DTO.OrderDetail;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDetailResponseDto {
    private Long id;
    private String orderId;
    private String productId;
    private Integer qty;
    private Double unitPrice;
    private Double subTotal;
    private String otherMethodId;
    private String paymentMethodId;
    /** Name of the product on this line (so the app can show it instead of an id). */
    private String productName;
    private Double commissionRate;
    private Double commissionAmount;
    private Double farmerNetAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
