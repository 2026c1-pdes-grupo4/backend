package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PropertyIntegrationTest extends BaseIntegrationTest {

    @Test
    void buyerPuedeListarTodasLasPropiedades() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void buyerPuedeBuscarPropiedadesPorCiudad() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("city", "Buenos Aires")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void buyerNoPuedeCrearUnaPropiedad() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "HOUSE",
                                "price", 100000,
                                "address", "Calle Falsa 123",
                                "city", "Buenos Aires",
                                "province", "Buenos Aires",
                                "areaSq", 80,
                                "rooms", 3
                        ))))
                .andExpect(status().isForbidden());
    }
}

