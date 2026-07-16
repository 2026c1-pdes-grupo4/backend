package ar.edu.unq.backend.agency;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AgencyPropertyRepository agencyPropertyRepository;
    @Mock
    private JwtAuthUtils jwtAuthUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AgencyMapper agencyMapper;

    @InjectMocks
    private AgencyService agencyService;

    @Test
    void createThrowsWhenUsernameExists() {
        AgencyRequestDTO dto = new AgencyRequestDTO();
        dto.setUsername("inmo");
        dto.setEmail("inmo@cth.com");

        when(agencyRepository.existsByUsername("inmo")).thenReturn(true);

        assertThrows(ConflictException.class, () -> agencyService.create(dto));
    }

    @Test
    void createAssignsAdminUser() {
        AgencyRequestDTO dto = new AgencyRequestDTO();
        dto.setUsername("inmo");
        dto.setEmail("inmo@cth.com");
        dto.setPassword("1234");

        User admin = new User();
        admin.setUserId(9);

        when(agencyRepository.existsByUsername("inmo")).thenReturn(false);
        when(agencyRepository.existsByEmail("inmo@cth.com")).thenReturn(false);
        when(jwtAuthUtils.getCurrentId()).thenReturn(9);
        when(userRepository.findById(9)).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode("1234")).thenReturn("ENC");
        when(agencyRepository.save(any(Agency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agencyMapper.mapToResponse(any(Agency.class))).thenReturn(new AgencyResponseDTO());

        agencyService.create(dto);

        ArgumentCaptor<Agency> captor = ArgumentCaptor.forClass(Agency.class);
        verify(agencyRepository).save(captor.capture());
        assertEquals(9, captor.getValue().getAdminUser().getUserId());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(agencyRepository.findById(33)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyService.findById(33));
    }

    @Test
    void updateThrowsWhenEmailAlreadyExists() {
        Agency agency = new Agency();
        agency.setAgencyId(1);
        agency.setUsername("inmo");
        agency.setEmail("old@cth.com");

        AgencyRequestDTO dto = new AgencyRequestDTO();
        dto.setUsername("inmo");
        dto.setEmail("new@cth.com");

        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyRepository.existsByEmail("new@cth.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> agencyService.update(1, dto));
    }

    @Test
    void updateSkipsPasswordEncodingWhenPasswordBlank() {
        Agency agency = new Agency();
        agency.setAgencyId(1);
        agency.setUsername("inmo");
        agency.setEmail("old@cth.com");

        AgencyRequestDTO dto = new AgencyRequestDTO();
        dto.setUsername("inmo");
        dto.setEmail("old@cth.com");
        dto.setPassword(" ");

        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyRepository.save(any(Agency.class))).thenReturn(agency);
        when(agencyMapper.mapToResponse(agency)).thenReturn(new AgencyResponseDTO());

        agencyService.update(1, dto);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteSoftDeletesAgency() {
        Agency agency = new Agency();
        agency.setAgencyId(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyPropertyRepository.findByAgency_AgencyIdAndDeletedFalse(1)).thenReturn(List.of());

        agencyService.delete(1);

        assertTrue(agency.getDeleted());
        verify(agencyRepository).save(agency);
        verify(agencyRepository, never()).delete(any(Agency.class));
    }

    @Test
    void deleteCascadesToActiveListings() {
        Agency agency = new Agency();
        agency.setAgencyId(1);

        AgencyProperty listing1 = new AgencyProperty();
        AgencyProperty listing2 = new AgencyProperty();

        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(agencyPropertyRepository.findByAgency_AgencyIdAndDeletedFalse(1)).thenReturn(List.of(listing1, listing2));

        agencyService.delete(1);

        assertTrue(listing1.getDeleted());
        assertTrue(listing2.getDeleted());
        verify(agencyPropertyRepository).saveAll(List.of(listing1, listing2));
    }
}

