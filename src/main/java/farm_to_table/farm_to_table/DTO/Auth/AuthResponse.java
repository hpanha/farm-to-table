package farm_to_table.farm_to_table.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Long roleId;
    private Long userId;
    private String approvalStatus;
}