package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.Repository.PlatformSettingRepository;
import farm_to_table.farm_to_table.model.PlatformSettingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Platform commission rate. Defaults to 5% until the admin sets it.
 * The rate is read by the order flow and snapshotted onto each order line when it is delivered,
 * so changing the rate later never changes completed orders.
 */
@Service
@RequiredArgsConstructor
public class CommissionService {

    static final String KEY = "commission_rate";

    /** Used until an admin sets a different rate. */
    public static final BigDecimal DEFAULT_RATE = new BigDecimal("0.05");

    private final PlatformSettingRepository repository;

    @Transactional(readOnly = true)
    public BigDecimal getRate() {
        return repository.findById(KEY)
                .map(PlatformSettingEntity::getSettingValue)
                .map(CommissionService::parseOrDefault)
                .orElse(DEFAULT_RATE);
    }

    @Transactional
    public BigDecimal setRate(BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Commission rate must be >= 0 and < 1 (e.g. 0.10 for 10%)");
        }
        PlatformSettingEntity entity = repository.findById(KEY).orElseGet(() -> {
            PlatformSettingEntity e = new PlatformSettingEntity();
            e.setSettingKey(KEY);
            return e;
        });
        entity.setSettingValue(rate.stripTrailingZeros().toPlainString());
        repository.save(entity);
        return rate;
    }

    /** Money rounding used for commission amounts: 2 decimals, HALF_UP. */
    public static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal parseOrDefault(String raw) {
        try {
            return new BigDecimal(raw.trim());
        } catch (RuntimeException e) {
            return DEFAULT_RATE;
        }
    }
}
