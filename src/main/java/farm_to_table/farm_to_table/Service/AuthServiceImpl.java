package farm_to_table.farm_to_table.Service;

import farm_to_table.farm_to_table.Config.JwtUtils;
import farm_to_table.farm_to_table.DTO.Auth.AuthResponse;
import farm_to_table.farm_to_table.DTO.Login.LoginRequest;
import farm_to_table.farm_to_table.DTO.Register.RegisterRequest;
import farm_to_table.farm_to_table.Repository.RoleRepository;
import farm_to_table.farm_to_table.Repository.UserRepository;
import farm_to_table.farm_to_table.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        Long roleId;
        try {
            roleId = Long.parseLong(request.getRoleId());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid roleId");
        }
        if (!roleRepository.existsById(roleId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");

        UserEntity u = new UserEntity();
        u.setUsername(request.getUsername());
        u.setFullName(request.getFullName());
        u.setEmail(request.getEmail());
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setStatus("ACTIVE");
        u.setRolesId(roleId);
        userRepository.save(u);
        return "Register success";
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity u = findByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        String token = jwtUtils.generateToken(u.getUsername());
        return new AuthResponse(token, u.getUsername(), u.getRolesId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findByUsername(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}