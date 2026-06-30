package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.ForbiddenException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyPropertyServiceTest {

    @Mock
    private AgencyPropertyRepository agencyPropertyRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private AgencyPropertyMapper agencyPropertyMapper;
    @Mock
    private JwtAuthUtils jwtAuthUtils;

    @InjectMocks
    private AgencyPropertyService agencyPropertyService;

    private Agency agency;
    private Property property;
    private AgencyProperty agencyProperty;
    private AgencyPropertyRequestDTO dto;

    @BeforeEach
    void setUp() {
        agency = new Agency();
        agency.setAgencyId(1);

        property = new Property();
        property.setPropertyId(2);
        property.setAvailable(true);

        agencyProperty = new AgencyProperty();
        agencyProperty.setAgencyPropertyId(10);
        agencyProperty.setAgency(agency);
        agencyProperty.setProperty(property);
        agencyProperty.setListedPrice(100000.0);

        dto = new AgencyPropertyRequestDTO();
        dto.setPropertyId(2);
        dto.setListedPrice(BigDecimal.valueOf(100000));
    }

    @Test
    void publish_throwsNotFoundException_whenAgencyNotFound() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_throwsNotFoundException_whenPropertyNotFound() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_throwsValidationException_whenListedPriceIsNull() {
        dto.setListedPrice(null);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));

        assertThrows(ValidationException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_throwsValidationException_whenListedPriceIsZero() {
        dto.setListedPrice(BigDecimal.ZERO);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));

        assertThrows(ValidationException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_throwsValidationException_whenListedPriceIsNegative() {
        dto.setListedPrice(BigDecimal.valueOf(-500));
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));

        assertThrows(ValidationException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_throwsConflictException_whenPropertyAlreadyPublishedByAgency() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyId(1, 2)).thenReturn(true);

        assertThrows(ConflictException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_returnsResponse_whenValid() {
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyId(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        AgencyPropertyResponseDTO result = agencyPropertyService.publish(dto);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(agencyPropertyRepository).save(any(AgencyProperty.class));
        assertTrue(property.getAvailable());
    }

    @Test
    void findById_throwsNotFoundException_whenPublicationDoesNotExist() {
        when(agencyPropertyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.findById(99));
    }

    @Test
    void findById_returnsMappedDTO_whenFound() {
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        AgencyPropertyResponseDTO result = agencyPropertyService.findById(10);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void findByCurrentAgency_returnsEmptyList_whenNoPublications() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findByAgency_AgencyId(1)).thenReturn(List.of());

        List<AgencyPropertyResponseDTO> result = agencyPropertyService.findByCurrentAgency();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCurrentAgency_returnsMappedList_whenPublicationsExist() {
        AgencyPropertyResponseDTO responseDTO = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findByAgency_AgencyId(1)).thenReturn(List.of(agencyProperty));
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(responseDTO);

        List<AgencyPropertyResponseDTO> result = agencyPropertyService.findByCurrentAgency();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.getFirst());
    }

    @Test
    void updatePrice_throwsNotFoundException_whenPublicationDoesNotExist() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.updatePrice(99, dto));
    }

    @Test
    void updatePrice_throwsForbiddenException_whenAgencyIsNotOwner() {
        Agency otherAgency = new Agency();
        otherAgency.setAgencyId(2);
        agencyProperty.setAgency(otherAgency);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ForbiddenException.class, () -> agencyPropertyService.updatePrice(10, dto));
    }

    @Test
    void updatePrice_throwsValidationException_whenNewPriceIsNull() {
        dto.setListedPrice(null);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ValidationException.class, () -> agencyPropertyService.updatePrice(10, dto));
    }

    @Test
    void updatePrice_throwsValidationException_whenNewPriceIsZero() {
        dto.setListedPrice(BigDecimal.ZERO);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ValidationException.class, () -> agencyPropertyService.updatePrice(10, dto));
    }

    @Test
    void updatePrice_updatesPrice_whenOwnerAndValidPrice() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        AgencyPropertyResponseDTO result = agencyPropertyService.updatePrice(10, dto);

        assertEquals(200000.0, agencyProperty.getListedPrice());
        assertEquals(expectedResponse, result);
        verify(agencyPropertyRepository).save(agencyProperty);
    }

    @Test
    void delete_throwsNotFoundException_whenPublicationDoesNotExist() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.delete(99));
    }

    @Test
    void delete_throwsForbiddenException_whenAgencyIsNotOwner() {
        Agency otherAgency = new Agency();
        otherAgency.setAgencyId(2);
        agencyProperty.setAgency(otherAgency);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ForbiddenException.class, () -> agencyPropertyService.delete(10));
    }

    @Test
    void delete_throwsValidationException_whenPropertyIsSold() {
        property.setAvailable(false);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ValidationException.class, () -> agencyPropertyService.delete(10));
    }

    @Test
    void delete_deletesSuccessfully_whenOwnerAndPropertyAvailable() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        agencyPropertyService.delete(10);

        verify(agencyPropertyRepository).delete(agencyProperty);
    }
}
