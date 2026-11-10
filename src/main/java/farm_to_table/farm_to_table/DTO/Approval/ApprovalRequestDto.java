package farm_to_table.farm_to_table.DTO.Approval;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ApprovalRequestDto(
        @NotBlank
        @Pattern(regexp = "APPROVED|PENDING|REJECTED", message = "status must be APPROVED, PENDING or REJECTED")
        String status
) {}
