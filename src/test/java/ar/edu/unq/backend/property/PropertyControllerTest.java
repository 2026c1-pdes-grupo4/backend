package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency_property.AgencyPropertyResponseDTO;
import ar.edu.unq.backend.auth.JwtService;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.config.SecurityConfig;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@Import(SecurityConfig.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropertyService propertyService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void listReturns200WithArray() throws Exception {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(1);
        dto.setAddress("Av. Siempre Viva 742");

        when(propertyService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].address").value("Av. Siempre Viva 742"));
    }

    @Test
    void listReturns403WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/properties"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getReturns200WithBody() throws Exception {
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(5);
        dto.setCity("Buenos Aires");

        when(propertyService.findById(5)).thenReturn(dto);

        mockMvc.perform(get("/properties/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.city").value("Buenos Aires"));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void createReturns201WithBody() throws Exception {
        PropertyRequestDTO req = new PropertyRequestDTO();
        req.setAddress("Calle Falsa 123");
        req.setCity("Córdoba");

        PropertyResponseDTO resp = new PropertyResponseDTO();
        resp.setId(10);
        resp.setAddress("Calle Falsa 123");
        resp.setCity("Córdoba");

        when(propertyService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.address").value("Calle Falsa 123"));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void createReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(post("/properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void updateReturns200WithBody() throws Exception {
        PropertyRequestDTO req = new PropertyRequestDTO();
        req.setAddress("Nueva Dirección 1");

        PropertyResponseDTO resp = new PropertyResponseDTO();
        resp.setId(3);
        resp.setAddress("Nueva Dirección 1");

        when(propertyService.update(eq(3), any())).thenReturn(resp);

        mockMvc.perform(put("/properties/3")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.address").value("Nueva Dirección 1"));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void deleteReturns204() throws Exception {
        doNothing().when(propertyService).delete(7);

        mockMvc.perform(delete("/properties/7").with(csrf()))
                .andExpect(status().isNoContent());

        verify(propertyService).delete(7);
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void deleteReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(delete("/properties/7").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void searchReturns200WithMatchingResults() throws Exception {
        AgencyPropertyResponseDTO dto = new AgencyPropertyResponseDTO();
        dto.setId(2);
        dto.setCity("Rosario");
        dto.setListedPrice(150000.0);

        when(propertyService.search(eq("Rosario"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/properties/search").param("city", "Rosario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].city").value("Rosario"))
                .andExpect(jsonPath("$[0].listedPrice").value(150000));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findByCadastralReturns200WithBodyWhenFound() throws Exception {
        PropertyResponseDTO resp = new PropertyResponseDTO();
        resp.setId(4);
        resp.setAddress("San Martin 400");

        when(propertyService.findByCadastral("1", "A", "10", "5")).thenReturn(resp);

        mockMvc.perform(get("/properties/find-by-cadastral")
                        .param("circumscription", "1")
                        .param("section", "A")
                        .param("block", "10")
                        .param("parcel", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.address").value("San Martin 400"));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findByCadastralReturns404WhenNoMatch() throws Exception {
        when(propertyService.findByCadastral("1", "A", "10", "5"))
                .thenThrow(new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found"));

        mockMvc.perform(get("/properties/find-by-cadastral")
                        .param("circumscription", "1")
                        .param("section", "A")
                        .param("block", "10")
                        .param("parcel", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void findByCadastralReturns403WhenNotAgency() throws Exception {
        mockMvc.perform(get("/properties/find-by-cadastral")
                        .param("circumscription", "1")
                        .param("section", "A")
                        .param("block", "10")
                        .param("parcel", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void searchReturnsEmptyListWhenNoMatch() throws Exception {
        when(propertyService.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/properties/search").param("city", "NoExiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}

