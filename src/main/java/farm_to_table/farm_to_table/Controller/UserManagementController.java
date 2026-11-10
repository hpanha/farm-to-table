package farm_to_table.farm_to_table.Controller;

import farm_to_table.farm_to_table.DTO.CreateUser.CreateUserRequest;
import farm_to_table.farm_to_table.DTO.Response.UserResponse;
import farm_to_table.farm_to_table.DTO.UpdateUser.UpdateUserRequest;
import farm_to_table.farm_to_table.Service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // GET /api/users?page=0&size=10&sort=id,desc
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // PATCH /api/users/{id}/approval  body: { "status": "APPROVED" | "PENDING" | "REJECTED" }  (ADMIN only)
    @PatchMapping("/{id}/approval")
    public ResponseEntity<farm_to_table.farm_to_table.DTO.Approval.ApprovalResponseDto> setApproval(
            @PathVariable Long id,
            @Valid @RequestBody farm_to_table.farm_to_table.DTO.Approval.ApprovalRequestDto request) {
        return ResponseEntity.ok(userService.setApproval(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}