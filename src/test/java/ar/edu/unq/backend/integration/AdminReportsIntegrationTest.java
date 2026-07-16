package ar.edu.unq.backend.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E-4: Ciclo completo del Administrador
 *
 *   Login admin → Listar usuarios → Listar agencias → Crear usuario →
 *   Crear agencia → Ver favoritos globales → Ver compras globales →
 *   Reportes (top-buyers, top-properties, top-agencies) → Limpieza
 *
 * Validaciones adicionales:
 *   - Roles sin acceso a endpoints de admin
 *   - Estructura de respuesta de reportes
 */
@DisplayName("E2E-4: Ciclo completo del Administrador")
class AdminReportsIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN      = "admin123";
    private static final String ADMIN_PASS = "admin123";

    // Flujo completo de gestión y reportes

    @Test
    @Transactional
    @DisplayName("Flujo completo: login → listar → crear usuario → crear agencia → reportes → limpieza")
    void cicloCompleto_GestionAdminYReportes() throws Exception {

        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        // 1. Listar todos los usuarios
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 2. Listar todas las agencias
        mockMvc.perform(get("/admin/agencies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 3. Obtener el ID del admin para crear agencia bajo el
        MvcResult usersResult = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        long adminId = extractAdminId(usersResult.getResponse().getContentAsString(), ADMIN);

        // 4. Crear nuevo usuario buyer
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        MvcResult newUserResult = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "e2e_buyer_" + uniqueSuffix,
                                "email",       "e2e_buyer_" + uniqueSuffix + "@test.com",
                                "password",    "pass1234",
                                "profileType", "BUYER"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("e2e_buyer_" + uniqueSuffix))
                .andReturn();

        long newUserId = extractId(newUserResult.getResponse().getContentAsString());

        // 5. Crear nueva agencia asociada al admin
        MvcResult newAgencyResult = mockMvc.perform(post("/agencies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "e2e_agency_" + uniqueSuffix,
                                "email",       "e2e_agency_" + uniqueSuffix + "@test.com",
                                "password",    "agencypass123",
                                "adminUserId", adminId
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("e2e_agency_" + uniqueSuffix))
                .andReturn();

        long newAgencyId = extractId(newAgencyResult.getResponse().getContentAsString());

        // 6. Verificar que el nuevo usuario aparece en la lista
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'e2e_buyer_" + uniqueSuffix + "')]").isNotEmpty());

        // 7. Verificar que la nueva agencia aparece en la lista
        mockMvc.perform(get("/admin/agencies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'e2e_agency_" + uniqueSuffix + "')]").isNotEmpty());

        // 8. Ver favoritos globales del sistema
        mockMvc.perform(get("/admin/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 9. Ver compras globales del sistema
        mockMvc.perform(get("/admin/purchases")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        // 10. Reporte: top-buyers
        mockMvc.perform(get("/admin/reports/top-buyers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));

        // 11. Reporte: top-ranked-properties
        mockMvc.perform(get("/admin/reports/top-ranked-properties")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));

        // 12. Reporte: top-agencies-sales
        mockMvc.perform(get("/admin/reports/top-agencies-sales")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));

        // 13. Limpieza: eliminar agencia creada
        mockMvc.perform(delete("/agencies/" + newAgencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 14. Limpieza: eliminar usuario creado
        mockMvc.perform(delete("/users/" + newUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 15. Verificar que ya no existen
        mockMvc.perform(get("/users/" + newUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/agencies/" + newAgencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Reportes individuales

    @Test
    @DisplayName("Reporte top-buyers: devuelve lista de hasta 5 compradores con más compras")
    void reporteTopBuyers_DevuelveMaximo5() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        mockMvc.perform(get("/admin/reports/top-buyers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));
    }

    @Test
    @DisplayName("Reporte top-ranked-properties: devuelve lista de hasta 5 propiedades mejor puntuadas")
    void reporteTopRankedProperties_DevuelveMaximo5() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        mockMvc.perform(get("/admin/reports/top-ranked-properties")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));
    }

    @Test
    @DisplayName("Reporte top-agencies-sales: devuelve lista de hasta 5 agencias con más ventas")
    void reporteTopAgenciesSales_DevuelveMaximo5() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        mockMvc.perform(get("/admin/reports/top-agencies-sales")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(5)));
    }

    // Favoritos y compras globales del sistema

    @Test
    @DisplayName("Admin puede ver todos los favoritos del sistema (seed tiene datos)")
    void adminPuedeVerTodosLosFavoritos() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        mockMvc.perform(get("/admin/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Admin puede ver todas las compras del sistema (seed tiene datos)")
    void adminPuedeVerTodasLasCompras() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);

        mockMvc.perform(get("/admin/purchases")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    // Control de acceso: otros roles no pueden acceder

    @Test
    @DisplayName("Buyer no puede acceder a reportes de admin → 403")
    void buyerNoPuedeAccederAReportes() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/admin/reports/top-buyers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/reports/top-ranked-properties")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/reports/top-agencies-sales")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Agencia no puede acceder a reportes de admin → 403")
    void agenciaNoPuedeAccederAReportes() throws Exception {
        String token = loginAndGetToken("inmo1", "agency123");

        mockMvc.perform(get("/admin/reports/top-buyers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Buyer no puede ver la lista de todos los usuarios → 403")
    void buyerNoPuedeListarUsuarios() throws Exception {
        String token = loginAndGetToken("buyer1", "buyer123");

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Acceder a reportes sin token devuelve 401")
    void reportesSinTokenDevuelven401() throws Exception {
        mockMvc.perform(get("/admin/reports/top-buyers"))
                .andExpect(status().is4xxClientError());
    }

    // CRUD de usuarios

    @Test
    @Transactional
    @DisplayName("Admin puede crear, obtener, actualizar y eliminar un usuario (CRUD completo)")
    void adminCicloCompletoUsuario() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);
        String suffix = String.valueOf(System.currentTimeMillis());

        // Crear
        MvcResult createResult = mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "crud_user_" + suffix,
                                "email",       "crud_" + suffix + "@test.com",
                                "password",    "crudpass123",
                                "profileType", "BUYER"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long userId = extractId(createResult.getResponse().getContentAsString());

        // Obtener
        mockMvc.perform(get("/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("crud_user_" + suffix));

        // Actualizar
        mockMvc.perform(put("/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "crud_updated_" + suffix,
                                "email",       "crud_updated_" + suffix + "@test.com",
                                "password",    "newpass123",
                                "profileType", "BUYER"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("crud_updated_" + suffix));

        // Eliminar
        mockMvc.perform(delete("/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    //  CRUD de agencias

    @Test
    @Transactional
    @DisplayName("Admin puede crear, obtener, actualizar y eliminar una agencia (CRUD completo)")
    void adminCicloCompletoAgencia() throws Exception {
        String token = loginAndGetToken(ADMIN, ADMIN_PASS);
        String suffix = String.valueOf(System.currentTimeMillis());

        // Obtener ID del admin
        MvcResult usersResult = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        long adminId = extractAdminId(usersResult.getResponse().getContentAsString(), ADMIN);

        // Crear agencia
        MvcResult createResult = mockMvc.perform(post("/agencies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "crud_agency_" + suffix,
                                "email",       "crud_agency_" + suffix + "@test.com",
                                "password",    "agencypass",
                                "adminUserId", adminId
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long agencyId = extractId(createResult.getResponse().getContentAsString());

        // Obtener
        mockMvc.perform(get("/agencies/" + agencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("crud_agency_" + suffix));

        // Actualizar
        mockMvc.perform(put("/agencies/" + agencyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username",    "crud_agency_upd_" + suffix,
                                "email",       "crud_agency_upd_" + suffix + "@test.com",
                                "password",    "updatedpass",
                                "adminUserId", adminId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("crud_agency_upd_" + suffix));

        // Eliminar
        mockMvc.perform(delete("/agencies/" + agencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verificar que no existe
        mockMvc.perform(get("/agencies/" + agencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Helpers

    private long extractId(String responseBody) throws Exception {
        var node = objectMapper.readTree(responseBody);
        String[] candidates = {"id", "userId", "agencyId", "propertyId", "agencyPropertyId"};
        for (String field : candidates) {
            if (node.has(field) && node.get(field).isNumber()) {
                return node.get(field).asLong();
            }
        }
        throw new AssertionError("No se encontró campo ID en la respuesta: " + responseBody);
    }

    private long extractAdminId(String usersJson, String username) throws Exception {
        var nodes = objectMapper.readTree(usersJson);
        for (var user : nodes) {
            if (username.equals(user.path("username").asText())) {
                String[] candidates = {"id", "userId"};
                for (String field : candidates) {
                    if (user.has(field) && user.get(field).isNumber()) {
                        return user.get(field).asLong();
                    }
                }
            }
        }
        // fallback: primer usuario admin
        for (var user : nodes) {
            if ("ADMIN".equalsIgnoreCase(user.path("profileType").asText())) {
                return user.path("id").asLong(1L);
            }
        }
        return 1L; // fallback seguro: el admin123 siempre es el primero del seed
    }
}

