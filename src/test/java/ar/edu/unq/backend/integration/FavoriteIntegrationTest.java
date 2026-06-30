package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteIntegrationTest extends BaseIntegrationTest {

    @Test
    void buyerPuedeVerSusFavoritos() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @Transactional
    void buyerPuedeAgregarUnFavorito() throws Exception {
        String token = loginAndGetToken("buyer2", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 5,
                                "score", 8,
                                "comment", "Excelente propiedad"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.comment").value("Excelente propiedad"));
    }

    @Test
    void agencyNoPuedeAccederAFavoritos() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

