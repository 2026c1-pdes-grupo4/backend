package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E-3: Ciclo completo de publicación de agencia
 *
 *   Login agencia → Crear propiedad → Publicar → Verificar en mis pubs →
 *   Actualizar precio → Verificar precio → Ver ventas → Eliminar pub (limpieza)
 *
 * Validaciones adicionales:
 *   - Doble publicación de la misma propiedad → 409
 *   - Agencia intenta modificar publicación de otra agencia → 403
 *   - Buyer no puede publicar → 403
 */
@DisplayName("E2E-3: Ciclo completo de Publicación de Agencia")
class AgencyPropertyIntegrationTest extends BaseIntegrationTest {

    private static final String AGENCY      = "inmo3";
    private static final String AGENCY_PASS = "agency123";

    // Flujo completo: Crear → Publicar → Actualizar → Eliminar

    @Test
    @Transactional
    @DisplayName("Flujo completo: crear propiedad → publicar → actualizar precio → verificar → eliminar")
    void cicloCompleto_CrearPublicarActualizarPrecioEliminar() throws Exception {

        String token = loginAndGetToken(AGENCY, AGENCY_PASS);

        // 1. Ver mis publicaciones actuales
        mockMvc.perform(get("/agency-properties/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 2. Crear nueva propiedad
        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "HOUSE",
                                "price", 250000,
                                "address", "Av. E2E Test 1234",
                                "city", "Córdoba",
                                "province", "Córdoba",
                                "areaSq", 100,
                                "rooms", 3,
                                "description", "Propiedad de test E2E"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long propertyId = extractId(propResult.getResponse().getContentAsString());

        // 3. Publicar la propiedad
        double listedPrice = 240000.0;
        MvcResult pubResult = mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", propertyId,
                                "listedPrice", listedPrice
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listedPrice").value(listedPrice))
                .andReturn();

        long agencyPropId = extractId(pubResult.getResponse().getContentAsString());

        // 4. Verificar que aparece en mis publicaciones
        mockMvc.perform(get("/agency-properties/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 5. Ver la publicación por ID
        mockMvc.perform(get("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listedPrice").value(listedPrice));

        // 6. Actualizar el precio
        double updatedPrice = 225000.0;
        mockMvc.perform(put("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listedPrice", updatedPrice
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listedPrice").value(updatedPrice));

        // 7. Verificar precio actualizado
        mockMvc.perform(get("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listedPrice").value(updatedPrice));

        // 8. Ver ventas de la agencia
        mockMvc.perform(get("/purchases/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 9. Eliminar la publicación
        mockMvc.perform(delete("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 10. Verificar que ya no existe
        mockMvc.perform(get("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Publicación duplicada (misma propiedad)

    @Test
    @Transactional
    @DisplayName("Agencia no puede publicar la misma propiedad dos veces → 409 PUBLICATION_ALREADY_EXISTS_FOR_AGENCY")
    void agenciaNoPuedeDuplicarPublicacion() throws Exception {
        String token = loginAndGetToken(AGENCY, AGENCY_PASS);

        // Crear propiedad
        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "APARTMENT",
                                "price", 180000,
                                "address", "Calle Duplicada 99",
                                "city", "Rosario",
                                "province", "Santa Fe",
                                "areaSq", 60,
                                "rooms", 2,
                                "description", "Test duplicación"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long propertyId = extractId(propResult.getResponse().getContentAsString());

        // Primera publicación → 201
        mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", propertyId,
                                "listedPrice", 175000.0
                        ))))
                .andExpect(status().isCreated());

        // Segunda publicación de la misma propiedad → 409
        mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", propertyId,
                                "listedPrice", 170000.0
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PUBLICATION_ALREADY_EXISTS_FOR_AGENCY"));
    }

    // Modificación de publicación de otra agencia
    @Test
    @Transactional
    @DisplayName("Agencia no puede modificar la publicación de otra agencia → 403")
    void agenciaNoPuedeModificarPublicacionDeOtraAgencia() throws Exception {

        // inmo1 crea y publica una propiedad
        String tokenInmo1 = loginAndGetToken("inmo1", AGENCY_PASS);

        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + tokenInmo1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "HOUSE",
                                "price", 300000,
                                "address", "Av. Propiedad Ajena 500",
                                "city", "Buenos Aires",
                                "province", "Buenos Aires",
                                "areaSq", 150,
                                "rooms", 4,
                                "description", "Propiedad de inmo1"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long propertyId = extractId(propResult.getResponse().getContentAsString());

        MvcResult pubResult = mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + tokenInmo1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", propertyId,
                                "listedPrice", 290000.0
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long agencyPropId = extractId(pubResult.getResponse().getContentAsString());

        // inmo2 intenta modificar la publicación de inmo1 → 403
        String tokenInmo2 = loginAndGetToken("inmo2", AGENCY_PASS);

        mockMvc.perform(put("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + tokenInmo2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "listedPrice", 100.0
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_MODIFY_OTHER_PUBLICATION"));

        // inmo2 intenta eliminar la publicación de inmo1 → 403
        mockMvc.perform(delete("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + tokenInmo2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_DELETE_OTHER_PUBLICATION"));
    }

    // Buyer no puede publicar

    @Test
    @DisplayName("Buyer no puede crear propiedades → 403")
    void buyerNoPuedeCrearPropiedades() throws Exception {
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

    @Test
    @DisplayName("Buyer no puede publicar una propiedad en una agencia → 403")
    void buyerNoPuedePublicarEnAgencia() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyId", 5,
                                "listedPrice", 100000.0
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Buyer no puede actualizar el precio de una publicación → 403")
    void buyerNoPuedeActualizarPrecio() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(put("/agency-properties/3")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("listedPrice", 1.0))))
                .andExpect(status().isForbidden());
    }

    // Eliminar publicación ya vendida

    @Test
    @Transactional
    @DisplayName("Agencia no puede eliminar una publicación que fue vendida → 400 SOLD_PUBLICATION_CANNOT_BE_DELETED")
    void agenciaNoPuedeEliminarPublicacionYaVendida() throws Exception {
        // inmo3 crea y publica una propiedad
        String agencyToken = loginAndGetToken(AGENCY, AGENCY_PASS);

        MvcResult propResult = mockMvc.perform(post("/properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "propertyType", "HOUSE", "price", 200000,
                                "address", "Calle Sold 99", "city", "Mendoza",
                                "province", "Mendoza", "areaSq", 80, "rooms", 3,
                                "description", "Test no se puede borrar si vendida"
                        ))))
                .andExpect(status().isCreated()).andReturn();
        long propertyId = extractId(propResult.getResponse().getContentAsString());

        MvcResult pubResult = mockMvc.perform(post("/agency-properties")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("propertyId", propertyId, "listedPrice", 195000.0))))
                .andExpect(status().isCreated()).andReturn();
        long agencyPropId = extractId(pubResult.getResponse().getContentAsString());

        // buyer12 compra esa publicación (ahora está vendida)
        String buyerToken = loginAndGetToken("buyer12", "buyer123");
        mockMvc.perform(post("/purchases")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("agencyPropertyId", agencyPropId))))
                .andExpect(status().isCreated());

        // inmo3 intenta eliminar la publicación ya vendida → 400 SOLD_PUBLICATION_CANNOT_BE_DELETED
        mockMvc.perform(delete("/agency-properties/" + agencyPropId)
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SOLD_PUBLICATION_CANNOT_BE_DELETED"));
    }

    // Ver publicaciones propias

    @Test
    @DisplayName("Agencia puede ver sus publicaciones actuales del seed")
    void agenciaPuedeVerSusPublicaciones() throws Exception {
        String token = loginAndGetToken("inmo1", AGENCY_PASS);

        mockMvc.perform(get("/agency-properties/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    // Helper
    private long extractId(String responseBody) throws Exception {
        var node = objectMapper.readTree(responseBody);
        String[] candidates = {"id", "propertyId", "agencyPropertyId", "userId", "agencyId"};
        for (String field : candidates) {
            if (node.has(field) && node.get(field).isNumber()) {
                return node.get(field).asLong();
            }
        }
        throw new AssertionError("No se encontró campo ID en la respuesta: " + responseBody);
    }
}

