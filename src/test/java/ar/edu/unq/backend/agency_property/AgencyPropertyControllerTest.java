package ar.edu.unq.backend.agency_property;

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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgencyPropertyController.class)
@Import(SecurityConfig.class)
class AgencyPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgencyPropertyService agencyPropertyService;

    @MockBean
    private JwtService jwtService;

    private AgencyPropertyResponseDTO buildResponse(int id, String address, String city) {
        AgencyPropertyResponseDTO dto = new AgencyPropertyResponseDTO();
        dto.setId(id);
        dto.setAddress(address);
        dto.setCity(city);
        dto.setPropertyType("APARTMENT");
        dto.setListedPrice(250000.0);
        dto.setListedDate(LocalDate.of(2026, 1, 10));
        dto.setAvailable(true);
        dto.setAgencyId(1);
        dto.setAgencyName("Inmo Test");
        return dto;
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void publishReturns201WithBody() throws Exception {
        AgencyPropertyRequestDTO req = new AgencyPropertyRequestDTO();
        req.setPropertyId(10);
        req.setListedPrice(BigDecimal.valueOf(250000));

        AgencyPropertyResponseDTO resp = buildResponse(1, "San Martin 400", "Mendoza");
        when(agencyPropertyService.publish(any())).thenReturn(resp);

        mockMvc.perform(post("/agency-properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.address").value("San Martin 400"))
                .andExpect(jsonPath("$.listedPrice").value(250000.0));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void publishReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(post("/agency-properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getReturns200WithBody() throws Exception {
        when(agencyPropertyService.findById(5)).thenReturn(buildResponse(5, "Urquiza 88", "Rosario"));

        mockMvc.perform(get("/agency-properties/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.address").value("Urquiza 88"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
      void getReturns403WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/agency-properties/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void myPublicationsReturns200WithArray() throws Exception {
        when(agencyPropertyService.findByCurrentAgency()).thenReturn(List.of(
                buildResponse(1, "Av. Siempre Viva 742", "Springfield"),
                buildResponse(2, "Pte. Peron 100", "Buenos Aires")
        ));

        mockMvc.perform(get("/agency-properties/agency/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void myPublicationsReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(get("/agency-properties/agency/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void updatePriceReturns200WithBody() throws Exception {
        AgencyPropertyRequestDTO req = new AgencyPropertyRequestDTO();
        req.setListedPrice(BigDecimal.valueOf(300000));

        AgencyPropertyResponseDTO resp = buildResponse(6, "Colon 55", "Cordoba");
        resp.setListedPrice(300000.0);
        when(agencyPropertyService.updatePrice(eq(6), any())).thenReturn(resp);

        mockMvc.perform(put("/agency-properties/6")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.listedPrice").value(300000.0));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void deleteReturns204() throws Exception {
        doNothing().when(agencyPropertyService).delete(7);

        mockMvc.perform(delete("/agency-properties/7").with(csrf()))
                .andExpect(status().isNoContent());

        verify(agencyPropertyService).delete(7);
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void deleteReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(delete("/agency-properties/7").with(csrf()))
                .andExpect(status().isForbidden());
    }
}

