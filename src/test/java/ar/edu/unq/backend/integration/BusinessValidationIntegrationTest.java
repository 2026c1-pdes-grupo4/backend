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
 * E2E-6 y E2E-7: Validaciones de Negocio y Conflictos
 *
 *   E2E-6 — Validaciones en búsqueda de propiedades (priceRange, propertyType, combinaciones)
 *   E2E-7 — Conflictos de datos (username/email duplicados, favorito duplicado, score inválido)
 *   E2E-5 — Validaciones de seguridad de roles adicionales
 *   Favoritos — Ciclo completo con validaciones de negocio incluidas
 */
@DisplayName("E2E-6 y E2E-7: Validaciones de Negocio y Conflictos")
class BusinessValidationIntegrationTest extends BaseIntegrationTest {

    // E2E-6: Validaciones en Búsqueda de Propiedades

    @Test
    @DisplayName("[E2E-6] Búsqueda con priceMin mayor a priceMax devuelve 400 INVALID_PRICE_RANGE")
    void busqueda_PrecioMinMayorQuePrecioMax_Devuelve400() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("priceMin", "500000")
                        .param("priceMax", "100000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRICE_RANGE"));
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda con tipo de propiedad inválido devuelve 400 INVALID_PROPERTY_TYPE")
    void busqueda_TipoInvalido_Devuelve400() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("propertyType", "CASTLE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PROPERTY_TYPE"));
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda sin filtros devuelve todas las propiedades disponibles")
    void busqueda_SinFiltros_DevuelvePropiedadesDisponibles() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda por ciudad devuelve resultados correctos")
    void busqueda_PorCiudad_DevuelveResultados() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("city", "Buenos Aires")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda combinada por tipo HOUSE y habitaciones filtra correctamente")
    void busqueda_Combinada_TipoYHabitaciones() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("propertyType", "HOUSE")
                        .param("rooms", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda con rango de precios válido devuelve resultados dentro del rango")
    void busqueda_ConRangoDePrecioValido_Devuelve200() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("priceMin", "50000")
                        .param("priceMax", "500000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda con priceMin igual a priceMax es válida")
    void busqueda_PrecioMinIgualAPrecioMax_Devuelve200() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("priceMin", "200000")
                        .param("priceMax", "200000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("[E2E-6] Búsqueda con tipo APARTMENT devuelve solo departamentos")
    void busqueda_TipoAPARTMENT_DevuelveSoloDepartamentos() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/properties/search")
                        .param("propertyType", "APARTMENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // E2E-7: Conflictos de Datos — Usuarios y Agencias

    @Test
    @Transactional
    @DisplayName("[E2E-7] Crear usuario con username ya existente devuelve 409 USERNAME_ALREADY_EXISTS")
    void crearUsuario_UsernameExistente_Devuelve409() throws Exception {
        String token = loginAndGetToken("admin123", "admin123");

        // "buyer1" ya existe en el seed
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "buyer1",
                                "email",       "buyer1_nuevo@test.com",
                                "password",    "pass123",
                                "profileType", "BUYER"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-7] Crear agencia con username ya existente devuelve 409 USERNAME_ALREADY_EXISTS")
    void crearAgencia_UsernameExistente_Devuelve409() throws Exception {
        String adminToken = loginAndGetToken("admin123", "admin123");

        // Obtener ID del admin
        long adminId = getAdminId(adminToken);

        // "inmo1" ya existe en el seed
        mockMvc.perform(post("/agencies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "inmo1",
                                "email",       "inmo1_nuevo@test.com",
                                "password",    "pass123",
                                "adminUserId", adminId
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"));
    }

    // E2E-7: Conflictos de Datos — Favoritos

    @Test
    @DisplayName("[E2E-7] Agregar favorito duplicado devuelve 409 PUBLICATION_ALREADY_FAVORITED")
    void favorito_Duplicado_Devuelve409() throws Exception {
        // buyer1 ya puso como fav a agencyPropertyId=1 en el seed
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 1,
                                "score",    7,
                                "comment",  "Intento duplicado"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PUBLICATION_ALREADY_FAVORITED"));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-7] Favorito con score negativo devuelve 400 INVALID_SCORE")
    void favorito_ScoreNegativo_Devuelve400() throws Exception {
        // buyer12 no tiene favoritos en seed → usamos listing 15
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 15,
                                "score",    -1,
                                "comment",  "Score inválido negativo"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SCORE"));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-7] Favorito con score mayor a 10 devuelve 400 INVALID_SCORE")
    void favorito_ScoreMayorA10_Devuelve400() throws Exception {
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 16,
                                "score",    11,
                                "comment",  "Score inválido mayor a 10"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SCORE"));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-7] Favorito con score 0 es válido (límite inferior)")
    void favorito_ScoreCeroEsValido() throws Exception {
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 17,
                                "score",    0,
                                "comment",  "Score mínimo válido"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(0));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-7] Favorito con score 10 es válido (límite superior)")
    void favorito_Score10EsValido() throws Exception {
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 18,
                                "score",    10,
                                "comment",  "Score máximo válido"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(10));
    }

    // E2E-2: Ciclo completo de Favoritos con validaciones
    @Test
    @Transactional
    @DisplayName("[E2E-2] Ciclo completo de favoritos: agregar → listar → actualizar → eliminar")
    void cicloCompleto_Favoritos() throws Exception {
        // buyer12 (no tiene favoritos en seed) + agencyPropertyId 11
        String token = loginAndGetToken("buyer12", "buyer123");

        // 1. Verificar historial vacío de favoritos
        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // 2. Agregar favorito con score válido - 201
        MvcResult createResult = mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 11,
                                "score",    7,
                                "comment",  "Me gustó mucho la propiedad"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(7))
                .andExpect(jsonPath("$.comment").value("Me gustó mucho la propiedad"))
                .andReturn();

        long favoriteId = extractId(createResult.getResponse().getContentAsString());

        // 3. Verificar que aparece en el listado
        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 4. Intentar agregar el mismo favorito de nuevo → 409
        mockMvc.perform(post("/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "agencyPropertyId", 11,
                                "score",    8,
                                "comment",  "Intento duplicado"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PUBLICATION_ALREADY_FAVORITED"));

        // 5. Actualizar score y comentario
        mockMvc.perform(put("/favorites/" + favoriteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "score",   9,
                                "comment", "Actualicé mi opinión, excelente propiedad"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9))
                .andExpect(jsonPath("$.comment").value("Actualicé mi opinión, excelente propiedad"));

        // 6. Verificar que el listado muestra el score actualizado
        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.score == 9)]").isNotEmpty());

        // 7. Eliminar el favorito
        mockMvc.perform(delete("/favorites/" + favoriteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 8. Verificar que el historial volvió a 0
        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-2] Buyer no puede modificar el favorito de otro buyer → 403")
    void favorito_BuyerNoPuedeModificarFavoritoDeOtroBuyer() throws Exception {
        // buyer1 tiene favorite_id=1 en seed (agencyPropertyId=1, score=8)
        // buyer12 intenta modificar ese favorito → 403
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(put("/favorites/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "score",   1,
                                "comment", "Intento de modificación no autorizado"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_MODIFY_OTHER_FAVORITE"));
    }

    @Test
    @Transactional
    @DisplayName("[E2E-2] Buyer no puede eliminar el favorito de otro buyer → 403")
    void favorito_BuyerNoPuedeEliminarFavoritoDeOtroBuyer() throws Exception {
        // buyer1 tiene favorite_id=1 en seed
        // buyer12 intenta eliminarlo → 403
        String token = loginAndGetToken("buyer12", "buyer123");

        mockMvc.perform(delete("/favorites/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CANNOT_DELETE_OTHER_FAVORITE"));
    }

    // E2E-5: Validaciones de Roles Adicionales

    @Test
    @DisplayName("[E2E-5] Agency no puede acceder a /favorites/me → 403")
    void agenciaNoPuedeAccederAFavoritos() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[E2E-5] Admin no puede acceder a /favorites/me → 403")
    void adminNoPuedeAccederAFavoritos() throws Exception {
        String token = loginAndGetToken("admin123", "admin123");

        mockMvc.perform(get("/favorites/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[E2E-5] Agency no puede ver /purchases/me (de buyers) → 403")
    void agenciaNoPuedeVerComprasDeCompradores() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/purchases/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[E2E-5] Buyer no puede ver /purchases/agency/me → 403")
    void buyerNoPuedeVerVentasDeAgencia() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/purchases/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[E2E-5] Buyer no puede ver /agency-properties/agency/me → 403")
    void buyerNoPuedeVerPublicacionesDeAgencia() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/agency-properties/agency/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[E2E-5] Request a endpoint protegido sin token devuelve 401")
    void sinToken_AccesoEndpointProtegido_Devuelve401() throws Exception {
        mockMvc.perform(get("/properties/search"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/favorites/me"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/purchases/me"))
                .andExpect(status().is4xxClientError());
    }

    // Helpers

    private long extractId(String responseBody) throws Exception {
        var node = objectMapper.readTree(responseBody);
        String[] candidates = {"id", "favoriteId", "purchaseId", "propertyId", "agencyPropertyId", "userId", "agencyId"};
        for (String field : candidates) {
            if (node.has(field) && node.get(field).isNumber()) {
                return node.get(field).asLong();
            }
        }
        throw new AssertionError("No se encontró campo ID en la respuesta: " + responseBody);
    }

    private long getAdminId(String adminToken) throws Exception {
        MvcResult usersResult = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        var nodes = objectMapper.readTree(usersResult.getResponse().getContentAsString());
        for (var user : nodes) {
            if ("admin123".equals(user.path("username").asText())) {
                if (user.has("id")) return user.get("id").asLong();
                if (user.has("userId")) return user.get("userId").asLong();
            }
        }
        return 1L;
    }
}

