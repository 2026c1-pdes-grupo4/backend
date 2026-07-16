package ar.edu.unq.backend.auth.service;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.auth.dto.LoginRequest;
import ar.edu.unq.backend.auth.dto.LoginResponse;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.UnauthorizedException;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servicio de autenticación que valida credenciales y genera tokens JWT
 * tanto para usuarios como para agencias.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, AgencyRepository agencyRepository,
                       PasswordEncoder encoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /**
     * Autentica a un usuario o agencia según el username.
     * Busca primero en la tabla de usuarios; si no se encuentra, busca en agencias.
     *
     * @param req datos de inicio de sesión (username y contraseña)
     * @return respuesta con el token JWT generado
     * @throws RuntimeException si las credenciales son inválidas
     */
    public LoginResponse login(LoginRequest req) {
        var user = userRepository.findByUsername(req.username());
        if (user.isPresent()) {
            return authenticateUser(user.get(), req.password());
        }

        var agency = agencyRepository.findByUsernameAndDeletedFalse(req.username())
                .orElseThrow(() -> {
                    log.error("Login failed: username not found in users or agencies. username={}", req.username());
                    return new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
                });

        return authenticateAgency(agency, req.password());
    }

    /**
     * Valida la contraseña de un usuario y genera su token JWT con el rol correspondiente.
     *
     * @param user        entidad del usuario a autenticar
     * @param rawPassword contraseña sin encriptar
     * @return respuesta con el token JWT del usuario
     * @throws RuntimeException si la contraseña no coincide
     */
    private LoginResponse authenticateUser(User user, String rawPassword) {
        validatePassword(rawPassword, user.getPassword());

        String role = (user.getProfileType() == ProfileType.ADMIN) ? "ROLE_ADMIN" : "ROLE_BUYER";

        String token = jwtService.createToken(user.getUsername(), Map.of(
                "typ", "USER",
                "id", user.getUserId(),
                "roles", List.of(role)
        ));
        log.info("Login successful. username={}, role={}", user.getUsername(), role);
        return new LoginResponse(token);
    }

    /**
     * Valida la contraseña de una agencia y genera su token JWT con el rol {@code ROLE_AGENCY}.
     *
     * @param agency      entidad de la agencia a autenticar
     * @param rawPassword contraseña sin encriptar
     * @return respuesta con el token JWT de la agencia
     * @throws RuntimeException si la contraseña no coincide
     */
    private LoginResponse authenticateAgency(Agency agency, String rawPassword) {
        validatePassword(rawPassword, agency.getPassword());

        String token = jwtService.createToken(agency.getUsername(), Map.of(
                "typ", "AGENCY",
                "id", agency.getAgencyId(),
                "roles", List.of("ROLE_AGENCY")
        ));
        log.info("Login successful. username={}, role=ROLE_AGENCY", agency.getUsername());
        return new LoginResponse(token);
    }

    /**
     * Verifica que la contraseña en texto plano coincida con el hash almacenado.
     *
     * @param rawPassword     contraseña sin encriptar
     * @param encodedPassword hash almacenado en base de datos
     * @throws RuntimeException si las contraseñas no coinciden
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!encoder.matches(rawPassword, encodedPassword)) {
            log.error("Login failed: password mismatch");
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }
    }
}
