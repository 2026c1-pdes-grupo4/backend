package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseIntegrationTest extends BaseIntegrationTest {

    @Test
    void buyerPuedeVerSusCompras() throws Exception {
        String token = loginAndGetToken("buyer2", "buyer123");

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @Transactional
    void buyerPuedeRealizarUnaCompra() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchasePrice").isNotEmpty());
    }

    @Test
    void adminNoPuedeAccederAPurchases() throws Exception {
        String token = loginAndGetToken("admin123", "admin123");

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

