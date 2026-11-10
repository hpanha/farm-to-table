package farm_to_table.farm_to_table.Controller;

import farm_to_table.farm_to_table.DTO.Order.OrderRequestDto;
import farm_to_table.farm_to_table.DTO.Order.OrderResponseDto;
import farm_to_table.farm_to_table.DTO.Order.OrderStatusDto;
import farm_to_table.farm_to_table.DTO.Order.OrderUpdateDto;
import farm_to_table.farm_to_table.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody OrderUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody OrderStatusDto dto) {
        return ResponseEntity.ok(service.updateStatus(id, dto));
    }

    /** POST /api/orders/{id}/dev-deliver: dev bypass that moves the order to DELIVERED (flag-gated). */
    @PostMapping("/{id}/dev-deliver")
    public ResponseEntity<OrderResponseDto> devDeliver(@PathVariable Long id,
                                                       org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(service.devDeliver(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // /api/orders/number/ORD-20261029-483921
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponseDto> getByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(service.getByOrderNumber(orderNumber));
    }

    // /api/orders?customerId=1&status=PENDING&keyword=ORD&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> search(
            @RequestParam(defaultValue = "") String customerId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.search(customerId, status, keyword, pageable));
    }
}
