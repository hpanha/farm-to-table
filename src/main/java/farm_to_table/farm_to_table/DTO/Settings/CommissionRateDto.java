package farm_to_table.farm_to_table.DTO.Settings;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** rate is a fraction: 0.10 = 10%. Valid range 0 (inclusive) to 1 (exclusive). */
public record CommissionRateDto(@NotNull BigDecimal rate) {}
