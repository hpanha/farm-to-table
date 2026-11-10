package farm_to_table.farm_to_table.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;     // បន្ថែមថ្មី

    private String fullName;
    private String email;
    private String passwordHash;
    private String status;
    private String image;
    private Long rolesId;

    /**
     * Farmer approval state: PENDING, APPROVED or REJECTED.
     * Null means "approved" (accounts created before this column existed).
     */
    private String approvalStatus;
}
