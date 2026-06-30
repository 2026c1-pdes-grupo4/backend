package ar.edu.unq.backend.favorite;

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

@WebMvcTest(FavoriteController.class)
@Import(SecurityConfig.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private JwtService jwtService;

    private FavoriteResponseDTO buildResponse(int id, String address, String agency) {
        FavoriteResponseDTO dto = new FavoriteResponseDTO();
        dto.setId(id);
        dto.setPropertyAddress(address);
        dto.setAgencyName(agency);
        dto.setScore(8);
        dto.setComment("Buena ubicación");
        dto.setSavedPrice(BigDecimal.valueOf(180000));
        dto.setSavedDate(LocalDate.of(2026, 3, 10));
        return dto;
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void saveReturns201WithBody() throws Exception {
        FavoriteRequestDTO req = new FavoriteRequestDTO();
        req.setAgencyPropertyId(5);
        req.setScore(8);
        req.setComment("Buena ubicación");

        FavoriteResponseDTO resp = buildResponse(1, "Rivadavia 2000", "Inmo Centro");
        when(favoriteService.save(any())).thenReturn(resp);

        mockMvc.perform(post("/favorites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.propertyAddress").value("Rivadavia 2000"))
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.agencyName").value("Inmo Centro"));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void saveReturns403WhenNotBuyer() throws Exception {
        mockMvc.perform(post("/favorites")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void myFavoritesReturns200WithArray() throws Exception {
        when(favoriteService.findForCurrentUser()).thenReturn(List.of(
                buildResponse(1, "Corrientes 500", "Inmo Sur"),
                buildResponse(2, "Lavalle 300", "Inmo Norte")
        ));

        mockMvc.perform(get("/favorites/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].propertyAddress").value("Corrientes 500"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void myFavoritesReturns403WhenNotBuyer() throws Exception {
        mockMvc.perform(get("/favorites/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void updateReturns200WithBody() throws Exception {
        FavoriteRequestDTO req = new FavoriteRequestDTO();
        req.setScore(9);
        req.setComment("Excelente propiedad");

        FavoriteResponseDTO resp = buildResponse(3, "Belgrano 100", "Inmo Oeste");
        resp.setScore(9);
        resp.setComment("Excelente propiedad");
        when(favoriteService.update(eq(3), any())).thenReturn(resp);

        mockMvc.perform(put("/favorites/3")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.score").value(9))
                .andExpect(jsonPath("$.comment").value("Excelente propiedad"));
    }

    @Test
    @WithMockUser(roles = "BUYER")
    void deleteReturns204() throws Exception {
        doNothing().when(favoriteService).delete(4);

        mockMvc.perform(delete("/favorites/4").with(csrf()))
                .andExpect(status().isNoContent());

        verify(favoriteService).delete(4);
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void deleteReturns403WhenNotBuyer() throws Exception {
        mockMvc.perform(delete("/favorites/4").with(csrf()))
                .andExpect(status().isForbidden());
    }
}

