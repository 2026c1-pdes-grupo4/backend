package ar.edu.unq.backend.auth.controller;

import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.auth.dto.LoginRequest;
import ar.edu.unq.backend.auth.dto.LoginResponse;
import ar.edu.unq.backend.auth.service.AuthService;
import ar.edu.unq.backend.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void loginReturns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest("usuario1", "pass123");
        LoginResponse resp = new LoginResponse("eyJhbGciOiJIUzI1NiJ9.mock.token");

        when(authService.login(any())).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.mock.token"));
    }

    @Test
    void loginIsPublicEndpoint() throws Exception {
        LoginRequest req = new LoginRequest("cualquier", "clave");
        when(authService.login(any())).thenReturn(new LoginResponse("fake-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"));
    }
}

