package ar.edu.unq.backend.user;

import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private UserResponseDTO buildResponse(int id, String username, String email) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setProfileType(ProfileType.BUYER);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WithBody() throws Exception {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("comprador1");
        req.setEmail("comprador@test.com");
        req.setPassword("pass");
        req.setProfileType(ProfileType.BUYER);

        UserResponseDTO resp = buildResponse(1, "comprador1", "comprador@test.com");
        when(userService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("comprador1"))
                .andExpect(jsonPath("$.email").value("comprador@test.com"))
                .andExpect(jsonPath("$.profileType").value("BUYER"));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void createReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listReturns200WithArray() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                buildResponse(1, "ana", "ana@test.com"),
                buildResponse(2, "bob", "bob@test.com")
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("ana"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void listReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReturns200WithBody() throws Exception {
        when(userService.findById(7)).thenReturn(buildResponse(7, "carlos", "carlos@test.com"));

        mockMvc.perform(get("/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("carlos"))
                .andExpect(jsonPath("$.profileType").value("BUYER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReturns200WithBody() throws Exception {
        UserRequestDTO req = new UserRequestDTO();
        req.setUsername("nuevo_nombre");
        req.setEmail("nuevo@test.com");

        UserResponseDTO resp = buildResponse(8, "nuevo_nombre", "nuevo@test.com");
        when(userService.update(eq(8), any())).thenReturn(resp);

        mockMvc.perform(put("/users/8")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.username").value("nuevo_nombre"))
                .andExpect(jsonPath("$.email").value("nuevo@test.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns204() throws Exception {
        doNothing().when(userService).delete(9);

        mockMvc.perform(delete("/users/9").with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(9);
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void deleteReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/users/9").with(csrf()))
                .andExpect(status().isForbidden());
    }
}

