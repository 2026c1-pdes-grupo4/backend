package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.agency.AgencyResponseDTO;
import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.config.SecurityConfig;
import ar.edu.unq.backend.user.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    // GET /admin/favorites

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFavoritesReturns200() throws Exception {
        when(adminService.findAllFavorites()).thenReturn(List.of());

        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void listFavoritesReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/admin/favorites"))
                .andExpect(status().isForbidden());
    }

    // GET /admin/purchases

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPurchasesReturns200() throws Exception {
        when(adminService.findAllPurchases()).thenReturn(List.of());

        mockMvc.perform(get("/admin/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // GET /admin/users

    @Test
    @WithMockUser(roles = "ADMIN")
    void usersReturns200WithBody() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1);
        user.setUsername("admin_user");
        user.setEmail("admin@test.com");

        when(adminService.findAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin_user"));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void usersReturns403WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    // GET /admin/agencies

    @Test
    @WithMockUser(roles = "ADMIN")
    void agenciesReturns200WithBody() throws Exception {
        AgencyResponseDTO agency = new AgencyResponseDTO();
        agency.setId(2);
        agency.setUsername("inmo_norte");
        agency.setEmail("norte@test.com");

        when(adminService.findAllAgencies()).thenReturn(List.of(agency));

        mockMvc.perform(get("/admin/agencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("inmo_norte"));
    }

    // GET /admin/reports/top-buyers

    @Test
    @WithMockUser(roles = "ADMIN")
    void topBuyersReturns200WithBody() throws Exception {
        TopBuyerDTO buyer = new TopBuyerDTO(10, "gran_comprador", 5L);
        when(adminService.topBuyers()).thenReturn(List.of(buyer));

        mockMvc.perform(get("/admin/reports/top-buyers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].username").value("gran_comprador"))
                .andExpect(jsonPath("$[0].purchases").value(5));
    }

    // GET /admin/reports/top-ranked-properties

    @Test
    @WithMockUser(roles = "ADMIN")
    void topRankedPropertiesReturns200WithBody() throws Exception {
        TopRankedPropertyDTO prop = new TopRankedPropertyDTO(3, "Av. Del Sol 100", 9.5, 12L);
        when(adminService.topRankedProperties()).thenReturn(List.of(prop));

        mockMvc.perform(get("/admin/reports/top-ranked-properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(3))
                .andExpect(jsonPath("$[0].address").value("Av. Del Sol 100"))
                .andExpect(jsonPath("$[0].averageScore").value(9.5));
    }

    // GET /admin/reports/top-agencies-sales

    @Test
    @WithMockUser(roles = "ADMIN")
    void topAgenciesBySalesReturns200WithBody() throws Exception {
        TopAgencySalesDTO sale = new TopAgencySalesDTO(7, "mega_inmo", 20L);
        when(adminService.topAgenciesBySales()).thenReturn(List.of(sale));

        mockMvc.perform(get("/admin/reports/top-agencies-sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].agencyId").value(7))
                .andExpect(jsonPath("$[0].username").value("mega_inmo"))
                .andExpect(jsonPath("$[0].sales").value(20));
    }
}

