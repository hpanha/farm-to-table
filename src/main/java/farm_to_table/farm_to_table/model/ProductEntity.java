package farm_to_table.farm_to_table.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tbl_product")
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String categoryId;
    private String name;
    private Integer qty;
    private Double unitPrice;
    private Double farmerPrice;

    @Temporal(TemporalType.DATE)
    private Date expectedHavestDate;

    @Temporal(TemporalType.DATE)
    private Date expiredDate;

    private String location;

    /** PENDING (waiting for approval), APPROVED, REJECTED. Null = legacy product, treated as APPROVED. */
    private String approvalStatus;
}
