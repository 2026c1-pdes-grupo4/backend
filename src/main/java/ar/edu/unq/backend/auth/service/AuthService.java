package ar.edu.unq.backend.auth.service;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.auth.dto.LoginRequest;
import ar.edu.unq.backend.auth.dto.LoginResponse;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

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

    public LoginResponse login(LoginRequest req) {
        var user = userRepository.findByUsername(req.username());
        if (user.isPresent()) {
            return authenticateUser(user.get(), req.password());
        }

        var agency = agencyRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        return authenticateAgency(agency, req.password());
    }

    private LoginResponse authenticateUser(User user, String rawPassword) {
        validatePassword(rawPassword, user.getPassword());

        String role = (user.getProfileType() == ProfileType.ADMIN) ? "ROLE_ADMIN" : "ROLE_BUYER";

        String token = jwtService.createToken(user.getUsername(), Map.of(
                "typ", "USER",
                "id", user.getUserId(),
                "roles", List.of(role)
        ));
        return new LoginResponse(token);
    }

    private LoginResponse authenticateAgency(Agency agency, String rawPassword) {
        validatePassword(rawPassword, agency.getPassword());

        String token = jwtService.createToken(agency.getUsername(), Map.of(
                "typ", "AGENCY",
                "id", agency.getAgencyId(),
                "roles", List.of("ROLE_AGENCY")
        ));
        return new LoginResponse(token);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!encoder.matches(rawPassword, encodedPassword)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }
}

