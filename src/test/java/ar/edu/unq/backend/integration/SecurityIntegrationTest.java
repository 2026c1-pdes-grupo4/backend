package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityIntegrationTest extends BaseIntegrationTest {

    @Test
    void adminPuedeAccederAGestionDeUsuarios() throws Exception {
        String token = loginAndGetToken("admin123", "admin123");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buyerNoPuedeAccederAEndpointsDeAdmin() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void solicitudSinTokenEsRechazada() throws Exception {
        mockMvc.perform(get("/properties"))
                .andExpect(status().is4xxClientError());
    }
}

