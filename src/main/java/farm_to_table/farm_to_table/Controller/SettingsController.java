package farm_to_table.farm_to_table.Controller;

import farm_to_table.farm_to_table.DTO.Settings.CommissionRateDto;
import farm_to_table.farm_to_table.Service.CommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final CommissionService commissionService;

    /** GET /api/settings/commission -> { "rate": 0 } (any logged-in user) */
    @GetMapping("/commission")
    public ResponseEntity<Map<String, Object>> getCommission() {
        return ResponseEntity.ok(Map.of("rate", commissionService.getRate()));
    }

    /**
     * PUT /api/settings/commission  body { "rate": 0.10 }
     * Allowed for ADMIN (ROLE_1). Any logged-in user may set it only when the dev bypass flag is on.
     */
    @PutMapping("/commission")
    public ResponseEntity<Map<String, Object>> setCommission(@Valid @RequestBody CommissionRateDto request,
                                                             Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_1".equals(a.getAuthority()));
        if (!admin && !devBypass) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only an ADMIN can change the commission rate");
        }
        return ResponseEntity.ok(Map.of("rate", commissionService.setRate(request.rate())));
    }

    /** Dev/test flag (same as the other bypasses). */
    @Value("${app.dev.bypass-approval:false}")
    private boolean devBypass;
}
