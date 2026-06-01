package ar.edu.unq.backend.agency;

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

@WebMvcTest(AgencyController.class)
@Import(SecurityConfig.class)
class AgencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgencyService agencyService;

    @MockBean
    private JwtService jwtService;

    private AgencyResponseDTO buildResponse(int id, String username, String email) {
        AgencyResponseDTO dto = new AgencyResponseDTO();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WithBody() throws Exception {
        AgencyRequestDTO req = new AgencyRequestDTO();
        req.setUsername("inmobiliaria_sur");
        req.setEmail("sur@email.com");
        req.setPassword("pass123");

        AgencyResponseDTO resp = buildResponse(1, "inmobiliaria_sur", "sur@email.com");
        when(agencyService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/agencies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("inmobiliaria_sur"))
                .andExpect(jsonPath("$.email").value("sur@email.com"));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void createReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/agencies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listReturns200WithArray() throws Exception {
        when(agencyService.findAll()).thenReturn(List.of(
                buildResponse(1, "norte", "norte@test.com"),
                buildResponse(2, "sur", "sur@test.com")
        ));

        mockMvc.perform(get("/agencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("norte"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void listReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/agencies"))
                .andExpect(status().isForbidden());
    }

    // GET /agencies/{id}

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReturns200WithBody() throws Exception {
        when(agencyService.findById(3)).thenReturn(buildResponse(3, "este", "este@test.com"));

        mockMvc.perform(get("/agencies/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("este"))
                .andExpect(jsonPath("$.email").value("este@test.com"));
    }

    // PUT /agencies/{id}

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateReturns200WithBody() throws Exception {
        AgencyRequestDTO req = new AgencyRequestDTO();
        req.setUsername("nuevo_nombre");
        req.setEmail("nuevo@email.com");

        AgencyResponseDTO resp = buildResponse(4, "nuevo_nombre", "nuevo@email.com");
        when(agencyService.update(eq(4), any())).thenReturn(resp);

        mockMvc.perform(put("/agencies/4")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nuevo_nombre"))
                .andExpect(jsonPath("$.email").value("nuevo@email.com"));
    }

    // DELETE /agencies/{id}

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns204() throws Exception {
        doNothing().when(agencyService).delete(5);

        mockMvc.perform(delete("/agencies/5").with(csrf()))
                .andExpect(status().isNoContent());

        verify(agencyService).delete(5);
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void deleteReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/agencies/5").with(csrf()))
                .andExpect(status().isForbidden());
    }
}

