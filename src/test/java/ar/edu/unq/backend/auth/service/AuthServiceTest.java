package ar.edu.unq.backend.auth.service;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.auth.dto.LoginRequest;
import ar.edu.unq.backend.auth.dto.LoginResponse;
import ar.edu.unq.backend.common.exception.UnauthorizedException;
import ar.edu.unq.backend.user.ProfileType;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginThrowsUnauthorizedForInvalidCredentials() {
        Agency agency = new Agency();
        agency.setPassword("ENC");

        when(userRepository.findByUsername("inmo")).thenReturn(Optional.empty());
        when(agencyRepository.findByUsername("inmo")).thenReturn(Optional.of(agency));
        when(encoder.matches("bad", "ENC")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("inmo", "bad")));
    }

    @Test
    void loginReturnsTokenForValidUser() {
        User user = new User();
        user.setUserId(3);
        user.setUsername("buyer");
        user.setPassword("ENC");
        user.setProfileType(ProfileType.BUYER);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
        when(encoder.matches("ok", "ENC")).thenReturn(true);
        when(jwtService.createToken(org.mockito.ArgumentMatchers.eq("buyer"), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn("TOKEN");

        LoginResponse response = authService.login(new LoginRequest("buyer", "ok"));

        assertEquals("TOKEN", response.token());
    }

    @Test
    void loginThrowsWhenNeitherUserNorAgencyExists() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(agencyRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("ghost", "none")));
    }

    @Test
    void loginReturnsTokenForValidAgency() {
        Agency agency = new Agency();
        agency.setAgencyId(10);
        agency.setUsername("inmo1");
        agency.setPassword("ENC");

        when(userRepository.findByUsername("inmo1")).thenReturn(Optional.empty());
        when(agencyRepository.findByUsername("inmo1")).thenReturn(Optional.of(agency));
        when(encoder.matches("ok", "ENC")).thenReturn(true);
        when(jwtService.createToken(org.mockito.ArgumentMatchers.eq("inmo1"), org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn("TOKEN_AGENCY");

        LoginResponse response = authService.login(new LoginRequest("inmo1", "ok"));

        assertEquals("TOKEN_AGENCY", response.token());
    }
}


