package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E-1: Ciclo completo de Compra
 *
 * Flujo (multi-rol):
 *   [Agency] Crear prop → Publicar
 *   [Buyer]  Login → Buscar → Ver detalle → Comprar →
 *            Verificar historial → intentar volver a comprar (→ 400 PROPERTY_ALREADY_SOLD)
 */
@DisplayName("E2E-1: Ciclo completo de Compra")
class FullPurchaseCycleIntegrationTest extends BaseIntegrationTest {

    private static final String BUYER       = "buyer12";
    private static final String BUYER_PASS  = "buyer123";
    private static final String AGENCY      = "inmo3";
    private static final String AGENCY_PASS = "agency123";

    // Flujo principal completo

    @Test
    @Transactional
    @DisplayName("Flujo completo multi-rol: agencia publica → buyer busca → compra → verifica historial → re-compra falla")
    void cicloCompleto_AgenciaPublicaBuyerCompraRecompraFalla() throws Exception {

        // [AGENCY] Crear y publicar propiedad
        String agencyToken = loginAndGetToken(AGENCY, AGENCY_PASS);
        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "HOUSE", "price", 300000,
                                "address", "Calle E2E Ciclo 1", "city", "Buenos Aires",
                                "province", "Buenos Aires", "areaSq", 120, "rooms", 4,
                                "description", "Test ciclo completo"
                        ))))
                .andExpect(status().isCreated()).andReturn();
        long propertyId = extractId(propResult.getResponse().getContentAsString());

        MvcResult pubResult = mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", propertyId, "listedPrice", 285000.0
                        ))))
                .andExpect(status().isCreated()).andReturn();
        long agencyPropId = extractId(pubResult.getResponse().getContentAsString());

        // Agencia ve sus publicaciones
        mockMvc.perform(get("/agency-properties/agency/me").header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // [BUYER] Login y flujo completo
        String buyerToken = loginAndGetToken(BUYER, BUYER_PASS);

        // 1. Buscar propiedades disponibles
        mockMvc.perform(get("/properties/search").param("city", "Buenos Aires")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 2. Ver detalle de la publicación
        mockMvc.perform(get("/agency-properties/" + agencyPropId).header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.listedPrice").value(285000.0));

        // 3. Realizar la compra → 201
        mockMvc.perform(post("/purchases").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", agencyPropId))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.purchasePrice").isNotEmpty());

        // 4. Verificar que la compra aparece en el historial
        mockMvc.perform(get("/purchases/me").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 5. intentar comprar la misma propiedad → 400 PROPERTY_ALREADY_SOLD
        mockMvc.perform(post("/purchases").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", agencyPropId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROPERTY_ALREADY_SOLD"));

        // 6. Agencia ve la venta en su historial
        mockMvc.perform(get("/purchases/agency/me").header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    // intentar comprar la propiedad vendida

    @Test
    @Transactional
    @DisplayName("Buyer no puede comprar una propiedad que ya fue vendida")
    void buyerNoPuedeComprarPropiedadYaVendida() throws Exception {
        String agencyToken = loginAndGetToken(AGENCY, AGENCY_PASS);
        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "APARTMENT", "price", 180000,
                                "address", "Av. Ya-Vendida 50", "city", "Córdoba",
                                "province", "Córdoba", "areaSq", 55, "rooms", 2,
                                "description", "Test ya vendida"
                        ))))
                .andExpect(status().isCreated()).andReturn();
        long propertyId = extractId(propResult.getResponse().getContentAsString());

        MvcResult pubResult = mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("propertyId", propertyId, "listedPrice", 170000.0))))
                .andExpect(status().isCreated()).andReturn();
        long agencyPropId = extractId(pubResult.getResponse().getContentAsString());

        String buyerToken = loginAndGetToken(BUYER, BUYER_PASS);

        mockMvc.perform(post("/purchases").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", agencyPropId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/purchases").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", agencyPropId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROPERTY_ALREADY_SOLD"));
    }

    // Control de acceso: Agency no puede comprar

    @Test
    @DisplayName("Una agencia no puede realizar compras → 403")
    void agenciaNoPuedeRealizarCompras() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", 5))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Una agencia no puede ver el endpoint /purchases/me de compradores → 403")
    void agenciaNoPuedeVerHistorialDeCompradores() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un admin no puede acceder a /purchases/me → 403")
    void adminNoPuedeVerHistorialDeCompradores() throws Exception {
        String token = loginAndGetToken("admin123", "admin123");

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // Ventas de agencia

    @Test
    @DisplayName("Una agencia puede ver las ventas de sus publicaciones")
    void agenciaPuedeVerSusVentas() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/purchases/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Un buyer no puede ver el endpoint /purchases/agency/me → 403")
    void buyerNoPuedeVerVentasDeAgencia() throws Exception {
        String token = loginAndGetToken(BUYER, BUYER_PASS);

        mockMvc.perform(get("/purchases/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // Comprar sin autenticación

    @Test
    @DisplayName("Realizar una compra sin token devuelve 401")
    void compraSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", 5))))
                .andExpect(status().is4xxClientError());
    }

    // Comprar con ID de publicación inexistente

    @Test
    @Transactional
    @DisplayName("Comprar con agencyPropertyId inexistente devuelve 404")
    void comprarConPublicacionInexistente() throws Exception {
        String token = loginAndGetToken(BUYER, BUYER_PASS);

        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", 999999))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AGENCY_PROPERTY_NOT_FOUND"));
    }

    // Historial de compras del seed

    @Test
    @DisplayName("buyer1 puede ver su historial de compras (tiene 1 compra en seed)")
    void buyer1TieneCompraEnSeed() throws Exception {
        String token = loginAndGetToken("buyer1", BUYER_PASS);

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    //  Helper
    private long extractId(String responseBody) throws Exception {
        var node = objectMapper.readTree(responseBody);
        String[] candidates = {"id", "propertyId", "agencyPropertyId", "purchaseId"};
        for (String field : candidates) {
            if (node.has(field) && node.get(field).isNumber()) {
                return node.get(field).asLong();
            }
        }
        throw new AssertionError("No se encontró campo ID en la respuesta: " + responseBody);
    }
}

