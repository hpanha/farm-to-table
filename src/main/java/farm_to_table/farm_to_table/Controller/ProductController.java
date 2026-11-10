package farm_to_table.farm_to_table.Controller;

import farm_to_table.farm_to_table.DTO.Approval.ApprovalRequestDto;
import farm_to_table.farm_to_table.DTO.Product.ProductRequest;
import jakarta.validation.Valid;
import farm_to_table.farm_to_table.DTO.Product.ProductResponse;
import farm_to_table.farm_to_table.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(productService.getById(id, viewerName(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(productService.getAll(viewerName(authentication)));
    }

    // PATCH /api/products/{id}/approval  body: { "status": "APPROVED" | "PENDING" | "REJECTED" }
    // ADMIN may set any product; the owner may only bypass-approve (APPROVED) when the dev flag is on.
    @PatchMapping("/{id}/approval")
    public ResponseEntity<ProductResponse> setApproval(@PathVariable Long id,
                                                       @Valid @RequestBody ApprovalRequestDto request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(productService.setApproval(id, request.status(), viewerName(authentication)));
    }

    private static String viewerName(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
