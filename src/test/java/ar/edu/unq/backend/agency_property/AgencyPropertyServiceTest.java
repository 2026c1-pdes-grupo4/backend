package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ForbiddenException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void publishThrowsWhenListedPriceIsInvalid() {
        AgencyPropertyRequestDTO dto = new AgencyPropertyRequestDTO();
        dto.setPropertyId(2);

        ar.edu.unq.backend.agency.Agency agency = new ar.edu.unq.backend.agency.Agency();
        agency.setAgencyId(1);
        Property property = new Property();
        property.setPropertyId(2);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));

        assertThrows(ValidationException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void updatePriceThrowsWhenPublicationFromAnotherAgency() {
        AgencyPropertyRequestDTO dto = new AgencyPropertyRequestDTO();
        dto.setListedPrice(java.math.BigDecimal.valueOf(10));

        ar.edu.unq.backend.agency.Agency owner = new ar.edu.unq.backend.agency.Agency();
        owner.setAgencyId(2);

        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(owner);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(11)).thenReturn(Optional.of(ap));

        assertThrows(ForbiddenException.class, () -> agencyPropertyService.updatePrice(11, dto));
    }

    @Test
    void deleteThrowsWhenPublicationIsSold() {
        ar.edu.unq.backend.agency.Agency owner = new ar.edu.unq.backend.agency.Agency();
        owner.setAgencyId(1);

        Property property = new Property();
        property.setAvailable(false);

        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(owner);
        ap.setProperty(property);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(ap));

        assertThrows(ValidationException.class, () -> agencyPropertyService.delete(10));
    }

    @Test
    void publishCreatesListingWhenValid() {
        AgencyPropertyRequestDTO dto = new AgencyPropertyRequestDTO();
        dto.setPropertyId(2);
        dto.setListedPrice(java.math.BigDecimal.valueOf(120000));

        ar.edu.unq.backend.agency.Agency agency = new ar.edu.unq.backend.agency.Agency();
        agency.setAgencyId(1);
        Property property = new Property();
        property.setPropertyId(2);

        AgencyProperty saved = new AgencyProperty();
        saved.setAgency(agency);
        saved.setProperty(property);
        saved.setListedPrice(120000.0);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyId(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(saved);
        when(agencyPropertyMapper.mapToResponse(saved)).thenReturn(new AgencyPropertyResponseDTO());

        assertNotNull(agencyPropertyService.publish(dto));
        verify(agencyPropertyRepository).save(any(AgencyProperty.class));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(agencyPropertyRepository.findById(123)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.findById(123));
    }

    @Test
    void updatePriceUpdatesWhenOwnerMatches() {
        AgencyPropertyRequestDTO dto = new AgencyPropertyRequestDTO();
        dto.setListedPrice(java.math.BigDecimal.valueOf(200));

        ar.edu.unq.backend.agency.Agency owner = new ar.edu.unq.backend.agency.Agency();
        owner.setAgencyId(1);

        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(owner);
        ap.setListedPrice(100.0);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(8)).thenReturn(Optional.of(ap));
        when(agencyPropertyRepository.save(ap)).thenReturn(ap);
        when(agencyPropertyMapper.mapToResponse(ap)).thenReturn(new AgencyPropertyResponseDTO());

        agencyPropertyService.updatePrice(8, dto);

        assertEquals(200.0, ap.getListedPrice());
    }
}


