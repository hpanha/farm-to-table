package farm_to_table.farm_to_table.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Key/value platform settings (e.g. commission_rate), editable from the admin portal. */
@Entity
@Table(name = "platform_setting")
@Getter
@Setter
public class PlatformSettingEntity extends BaseEntity {

    @Id
    @Column(name = "setting_key", length = 100)
    private String settingKey;

    @Column(name = "setting_value", length = 500)
    private String settingValue;
}
