package farm_to_table.farm_to_table.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class PermissionsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String permissionName;
    private String module;
    private String description;

    @Column(name = "can_insert") private Boolean canInsert;
    @Column(name = "can_read")   private Boolean canRead;
    @Column(name = "can_update") private Boolean canUpdate;
    @Column(name = "can_delete") private Boolean canDelete;
}
