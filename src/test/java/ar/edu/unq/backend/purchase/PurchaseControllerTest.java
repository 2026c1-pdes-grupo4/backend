package ar.edu.unq.backend.purchase;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@Import(SecurityConfig.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurchaseService purchaseService;

    @MockBean
    private JwtService jwtService;

    private PurchaseResponseDTO buildResponse(int id, String address, String agency) {
        PurchaseResponseDTO dto = new PurchaseResponseDTO();
        dto.setId(id);
        dto.setPropertyAddress(address);
        dto.setAgencyName(agency);
        dto.setPurchasePrice(BigDecimal.valueOf(200000));
        dto.setPurchaseDate(LocalDate.of(2026, 1, 15));
        return dto;
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void buyReturns201WithBody() throws Exception {
        PurchaseRequestDTO req = new PurchaseRequestDTO();
        req.setAgencyPropertyId(42);

        PurchaseResponseDTO resp = buildResponse(1, "Calle Falsa 123", "Inmo Norte");
        when(purchaseService.buy(any())).thenReturn(resp);

        mockMvc.perform(post("/purchases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.propertyAddress").value("Calle Falsa 123"))
                .andExpect(jsonPath("$.agencyName").value("Inmo Norte"))
                .andExpect(jsonPath("$.purchasePrice").value(200000));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void buyReturns403WhenNotBuyer() throws Exception {
        mockMvc.perform(post("/purchases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void myPurchasesReturns200WithArray() throws Exception {
        when(purchaseService.findForCurrentUser()).thenReturn(List.of(
                buildResponse(1, "Av. Corrientes 1234", "Inmo Sur"),
                buildResponse(2, "Pueyrredón 567", "Inmo Este")
        ));

        mockMvc.perform(get("/purchases/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].propertyAddress").value("Av. Corrientes 1234"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void myPurchasesReturns403WhenNotBuyer() throws Exception {
        mockMvc.perform(get("/purchases/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void agencySalesReturns200WithArray() throws Exception {
        when(purchaseService.findForCurrentAgency()).thenReturn(List.of(
                buildResponse(10, "Santa Fe 999", "Inmo Norte")
        ));

        mockMvc.perform(get("/purchases/agency/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].propertyAddress").value("Santa Fe 999"));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void agencySalesReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(get("/purchases/agency/me"))
                .andExpect(status().isForbidden());
    }
}

