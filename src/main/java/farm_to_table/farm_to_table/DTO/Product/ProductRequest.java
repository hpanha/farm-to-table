package farm_to_table.farm_to_table.DTO.Product;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String userId;
    private String categoryId;
    private String name;
    private Integer qty;
    private Double unitPrice;
    private Double farmerPrice;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedHavestDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiredDate;
    private String location;
    private java.util.List<String> images;
}