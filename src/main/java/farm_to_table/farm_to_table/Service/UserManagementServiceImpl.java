    package farm_to_table.farm_to_table.Service;

    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import farm_to_table.farm_to_table.DTO.CreateUser.CreateUserRequest;
    import farm_to_table.farm_to_table.DTO.Response.PermissionResponse;
    import farm_to_table.farm_to_table.DTO.Response.RoleResponse;
    import farm_to_table.farm_to_table.DTO.Response.UserAccountResponse;
    import farm_to_table.farm_to_table.DTO.Response.UserDetailResponse;
    import farm_to_table.farm_to_table.DTO.Response.UserResponse;
    import farm_to_table.farm_to_table.DTO.UpdateUser.UpdateUserRequest;
    import farm_to_table.farm_to_table.DTO.UserAccount.UserAccountRequest;
    import farm_to_table.farm_to_table.DTO.UserCompany.UserCompanyDto;
    import farm_to_table.farm_to_table.DTO.UserDetail.UserDetailRequest;
    import farm_to_table.farm_to_table.Repository.*;
    import farm_to_table.farm_to_table.model.UserAccountEntity;
    import farm_to_table.farm_to_table.model.UserCompanyEntity;
    import farm_to_table.farm_to_table.model.UserDetailEntity;
    import farm_to_table.farm_to_table.model.UserEntity;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.server.ResponseStatusException;

    import java.security.SecureRandom;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @Service
    @RequiredArgsConstructor
    public class UserManagementServiceImpl implements UserManagementService {

        private final UserRepository userRepository;
        private final UserDetailRepository detailRepository;
        private final UserCompanyRepository companyRepository;
        private final UserAccountRepository accountRepository;
        private final RoleRepository roleRepository;
        private final PermissionsRepository permissionsRepository;
        private final PasswordEncoder passwordEncoder;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        public UserResponse create(CreateUserRequest r) {
            if (userRepository.existsByEmail(r.email()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            if (userRepository.existsByUsername(r.username()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            if (!roleRepository.existsById(r.rolesId()))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");

            String tempPassword = generateTempPassword();

            Map<String, Object> payload = new HashMap<>();
            payload.put("username", r.username());
            payload.put("fullName", r.fullName());
            payload.put("email", r.email());
            payload.put("image", r.image());
            payload.put("rolesId", r.rolesId());
            payload.put("passwordHash", passwordEncoder.encode(tempPassword));
            payload.put("detail", r.detail());
            payload.put("company", r.company());
            payload.put("account", r.account());

            Long userId = userRepository.createUser(toJson(payload));
            return toResponse(findUser(userId), tempPassword);
        }

        @Override
        @Transactional(readOnly = true)
        public UserResponse getById(Long id) {
            return toResponse(findUser(id), null);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<UserResponse> getAll(Pageable pageable) {
            return userRepository.findByStatusNot("DELETED", pageable)
                    .map(u -> toResponse(u, null));
        }

        @Override
        @Transactional
        public UserResponse update(Long id, UpdateUserRequest r) {
            UserEntity u = findUser(id);

            if (r.fullName() != null) u.setFullName(r.fullName());
            if (r.image() != null)    u.setImage(r.image());
            if (r.status() != null)   u.setStatus(r.status());
            if (r.rolesId() != null) {
                if (!roleRepository.existsById(r.rolesId()))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
                u.setRolesId(r.rolesId());
            }
            userRepository.save(u);

            if (r.detail() != null) {
                UserDetailEntity d = detailRepository.findByUserId(id).orElseGet(() -> {
                    UserDetailEntity n = new UserDetailEntity();
                    n.setUserId(id);
                    return n;
                });
                UserDetailRequest x = r.detail();
                d.setNationality(x.nationality());
                d.setCardId(x.cardId());
                d.setAddress(x.address());
                d.setDateOfBirth(x.dateOfBirth());
                d.setPlaceOfBirth(x.placeOfBirth());
                d.setMaritalStatus(x.maritalStatus());
                detailRepository.save(d);
            }

            if (r.company() != null) {
                UserCompanyEntity c = companyRepository.findByUserId(id).orElseGet(() -> {
                    UserCompanyEntity n = new UserCompanyEntity();
                    n.setUserId(id);
                    return n;
                });
                UserCompanyDto x = r.company();
                c.setCompanyName(x.companyName());
                c.setAddress(x.address());
                c.setOperationYears(x.operationYears());
                c.setMap(x.map());
                c.setPartner(x.partner());
                c.setPhoneNumber(x.phoneNumber());
                c.setType(x.type());
                companyRepository.save(c);
            }

            if (r.account() != null) {
                UserAccountEntity a = accountRepository.findByUserId(id).stream()
                        .filter(acc -> Boolean.TRUE.equals(acc.getIsDefault()))
                        .findFirst()
                        .orElseGet(() -> {
                            UserAccountEntity n = new UserAccountEntity();
                            n.setUserId(id);
                            n.setIsDefault(true);
                            n.setIsVerified(false);
                            return n;
                        });
                UserAccountRequest x = r.account();
                a.setAccountName(x.accountName());
                a.setAccountType(x.accountType());
                a.setAccountNumber(x.accountNumber());
                a.setBankName(x.bankName());
                a.setIsVerified(false);
                accountRepository.save(a);
            }

            return toResponse(findUser(id), null);
        }

        @Override
        @Transactional
        public void delete(Long id) {
            UserEntity u = findUser(id);
            u.setStatus("DELETED");
            userRepository.save(u);
        }

        @Override
        @Transactional
        public farm_to_table.farm_to_table.DTO.Approval.ApprovalResponseDto setApproval(Long id, String status) {
            UserEntity u = findUser(id);
            if (u.getRolesId() == null || u.getRolesId() != 2L) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only FARMER accounts need approval");
            }
            u.setApprovalStatus(status);
            userRepository.save(u);
            return new farm_to_table.farm_to_table.DTO.Approval.ApprovalResponseDto(u.getId(), status);
        }

        private UserEntity findUser(Long id) {
            return userRepository.findById(id)
                    .filter(u -> !"DELETED".equals(u.getStatus()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        }

        private UserResponse toResponse(UserEntity u, String tempPassword) {
            RoleResponse role = roleRepository.findById(u.getRolesId())
                    .map(ro -> new RoleResponse(ro.getId(), ro.getName(),
                            permissionsRepository.findByRoleId(ro.getId()).stream()
                                    .map(p -> new PermissionResponse(p.getId(), p.getPermissionName(),
                                            p.getModule(), p.getCanInsert(), p.getCanRead(),
                                            p.getCanUpdate(), p.getCanDelete()))
                                    .toList()))
                    .orElse(null);

            UserDetailResponse detail = detailRepository.findByUserId(u.getId())
                    .map(d -> new UserDetailResponse(d.getId(), d.getUserId(), d.getNationality(),
                            d.getCardId(), d.getAddress(), d.getDateOfBirth(),
                            d.getPlaceOfBirth(), d.getMaritalStatus()))
                    .orElse(null);

            UserCompanyDto company = companyRepository.findByUserId(u.getId())
                    .map(c -> new UserCompanyDto(c.getCompanyName(), c.getAddress(),
                            c.getOperationYears(), c.getMap(), c.getPartner(),
                            c.getPhoneNumber(), c.getType()))
                    .orElse(null);

            List<UserAccountResponse> accounts = accountRepository.findByUserId(u.getId()).stream()
                    .map(a -> new UserAccountResponse(a.getId(), a.getAccountName(), a.getAccountType(),
                            mask(a.getAccountNumber()), a.getBankName(),
                            a.getIsVerified(), a.getIsDefault()))
                    .toList();

            return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(),
                    u.getStatus(), u.getImage(), role, detail, company, accounts,
                    u.getCreatedAt(), tempPassword);
        }

        private String toJson(Object o) {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Cannot serialize payload", e);
            }
        }

        private static String mask(String n) {
            return (n == null || n.length() <= 4) ? n : "****" + n.substring(n.length() - 4);
        }

        private static String generateTempPassword() {
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$";
            SecureRandom rnd = new SecureRandom();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            return sb.toString();
        }
    }