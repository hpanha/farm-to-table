package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.Product.ProductRequest;
import farm_to_table.farm_to_table.DTO.Product.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    /** @param viewerUsername the logged-in username, or null for guests */
    ProductResponse getById(Long id, String viewerUsername);
    /** @param actorUsername the logged-in user; owners may bypass-approve only when the dev flag is on */
    ProductResponse setApproval(Long id, String status, String actorUsername);
    List<ProductResponse> getAll(String viewerUsername);
}
