package farm_to_table.farm_to_table.Repository;

import farm_to_table.farm_to_table.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Page<UserEntity> findByStatusNot(String status, Pageable pageable);

    @Query(value = "SELECT * FROM sp_login_user(CAST(:username AS varchar))", nativeQuery = true)
    Optional<UserEntity> loginWithProcedure(@Param("username") String username);

    @Query(value = """
            SELECT sp_register_user(
                CAST(:username AS varchar), CAST(:fullName AS varchar), CAST(:email AS varchar),
                CAST(:passwordHash AS varchar), CAST(:companyName AS varchar),
                CAST(:phone AS varchar), CAST(:roleId AS bigint))
            """, nativeQuery = true)
    Long registerWithProcedure(@Param("username") String username,
                               @Param("fullName") String fullName,
                               @Param("email") String email,
                               @Param("passwordHash") String passwordHash,
                               @Param("companyName") String companyName,
                               @Param("phone") String phone,
                               @Param("roleId") Long roleId);

    @Query(value = "SELECT sp_create_user(CAST(:payload AS jsonb))", nativeQuery = true)
    Long createUser(@Param("payload") String payload);
}