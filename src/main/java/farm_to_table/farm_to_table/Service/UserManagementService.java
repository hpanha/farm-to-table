package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.DTO.CreateUser.CreateUserRequest;
import farm_to_table.farm_to_table.DTO.Response.UserResponse;
import farm_to_table.farm_to_table.DTO.UpdateUser.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserManagementService {
    UserResponse create(CreateUserRequest request);
    UserResponse getById(Long id);
    Page<UserResponse> getAll(Pageable pageable);
    UserResponse update(Long id, UpdateUserRequest request);
    void delete(Long id);
    farm_to_table.farm_to_table.DTO.Approval.ApprovalResponseDto setApproval(Long id, String status);
}