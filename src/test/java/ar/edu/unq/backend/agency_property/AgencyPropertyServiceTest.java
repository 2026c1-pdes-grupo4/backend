package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.ForbiddenException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.picture.PictureService;
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
import static org.mockito.Mockito.*;

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
    @Mock
    private PictureService pictureService;

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
        agencyProperty.setDeleted(false);

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
    void publish_throwsValidationException_whenPropertyIsAlreadySold() {
        property.setAvailable(false);
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
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(1, 2)).thenReturn(true);

        assertThrows(ConflictException.class, () -> agencyPropertyService.publish(dto));
    }

    @Test
    void publish_returnsResponse_whenValid() {
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn(null);

        AgencyPropertyResponseDTO result = agencyPropertyService.publish(dto);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(agencyPropertyRepository).save(any(AgencyProperty.class));
    }

    @Test
    void publish_savesImages_whenImageUrlsProvided() {
        String url = "https://img1.com/photo.jpg";
        dto.setImageUrl(url);

        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn(url);

        AgencyPropertyResponseDTO result = agencyPropertyService.publish(dto);

        assertNotNull(result);
        verify(pictureService).saveForListing(agencyProperty, url);
        assertEquals(url, result.getImageUrl());
    }

    @Test
    void publish_doesNotSaveImages_whenImageUrlsIsNull() {
        dto.setImageUrl(null);

        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn(null);

        agencyPropertyService.publish(dto);

        verify(pictureService).saveForListing(agencyProperty, null);
        assertNull(expectedResponse.getImageUrl());
    }

    @Test
    void publish_doesNotSaveImages_whenImageUrlsIsEmpty() {
        dto.setImageUrl("   ");

        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyRepository.findById(1)).thenReturn(Optional.of(agency));
        when(propertyRepository.findById(2)).thenReturn(Optional.of(property));
        when(agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(1, 2)).thenReturn(false);
        when(agencyPropertyRepository.save(any(AgencyProperty.class))).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn(null);

        agencyPropertyService.publish(dto);

        verify(pictureService).saveForListing(agencyProperty, "   ");
        assertNull(expectedResponse.getImageUrl());
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
        when(agencyPropertyRepository.findByAgency_AgencyIdAndDeletedFalse(1)).thenReturn(List.of());

        List<AgencyPropertyResponseDTO> result = agencyPropertyService.findByCurrentAgency();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCurrentAgency_returnsMappedList_whenPublicationsExist() {
        AgencyPropertyResponseDTO responseDTO = new AgencyPropertyResponseDTO();
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findByAgency_AgencyIdAndDeletedFalse(1)).thenReturn(List.of(agencyProperty));
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(responseDTO);

        List<AgencyPropertyResponseDTO> result = agencyPropertyService.findByCurrentAgency();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.getFirst());
    }

    @Test
    void update_throwsNotFoundException_whenPublicationDoesNotExist() {
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> agencyPropertyService.update(99, dto));
    }

    @Test
    void update_throwsForbiddenException_whenAgencyIsNotOwner() {
        Agency otherAgency = new Agency();
        otherAgency.setAgencyId(2);
        agencyProperty.setAgency(otherAgency);

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ForbiddenException.class, () -> agencyPropertyService.update(10, dto));
    }

    @Test
    void update_throwsValidationException_whenNewPriceIsNull() {
        dto.setListedPrice(null);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ValidationException.class, () -> agencyPropertyService.update(10, dto));
    }

    @Test
    void update_throwsValidationException_whenNewPriceIsZero() {
        dto.setListedPrice(BigDecimal.ZERO);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(ValidationException.class, () -> agencyPropertyService.update(10, dto));
    }

    @Test
    void update_updatesPrice_whenOwnerAndValidPrice() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        AgencyPropertyResponseDTO result = agencyPropertyService.update(10, dto);

        assertEquals(200000.0, agencyProperty.getListedPrice());
        assertEquals(expectedResponse, result);
        verify(agencyPropertyRepository).save(agencyProperty);
    }

    @Test
    void update_updatesImage_whenImageUrlProvided() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        dto.setImageUrl("https://new-image.com/photo.jpg");
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn("https://new-image.com/photo.jpg");

        AgencyPropertyResponseDTO result = agencyPropertyService.update(10, dto);

        verify(pictureService).updateForListing(agencyProperty, "https://new-image.com/photo.jpg");
        assertEquals("https://new-image.com/photo.jpg", result.getImageUrl());
    }

    @Test
    void update_doesNotChangeImage_whenImageUrlIsNull() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        dto.setImageUrl(null);
        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);
        when(pictureService.firstPictureUrl(10)).thenReturn("https://existing-image.com/photo.jpg");

        AgencyPropertyResponseDTO result = agencyPropertyService.update(10, dto);

        verify(pictureService).updateForListing(agencyProperty, null);
        assertEquals("https://existing-image.com/photo.jpg", result.getImageUrl());
    }

    @Test
    void update_updatesPropertyData_whenPropertyIsProvided() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        dto.setAddress("Nueva Dirección 456");
        dto.setRooms(5);
        dto.setCity("Córdoba");

        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        agencyPropertyService.update(10, dto);

        assertEquals("Nueva Dirección 456", property.getAddress());
        assertEquals(5, property.getRooms());
        assertEquals("Córdoba", property.getCity());
        verify(propertyRepository).save(property);
    }

    @Test
    void update_doesNotUpdatePropertyData_whenAllPropertyFieldsAreNull() {
        dto.setListedPrice(BigDecimal.valueOf(200000));
        // Todos los campos de propiedad son null por defecto

        AgencyPropertyResponseDTO expectedResponse = new AgencyPropertyResponseDTO();

        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));
        when(agencyPropertyRepository.save(agencyProperty)).thenReturn(agencyProperty);
        when(agencyPropertyMapper.mapToResponse(agencyProperty)).thenReturn(expectedResponse);

        agencyPropertyService.update(10, dto);

        verify(propertyRepository, never()).save(any());
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

        assertTrue(agencyProperty.getDeleted());
        verify(agencyPropertyRepository).save(agencyProperty);
    }

    @Test
    void findById_throwsNotFoundException_whenPublicationIsSoftDeleted() {
        agencyProperty.setDeleted(true);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(NotFoundException.class, () -> agencyPropertyService.findById(10));
    }

    @Test
    void update_throwsNotFoundException_whenPublicationIsSoftDeleted() {
        agencyProperty.setDeleted(true);
        when(jwtAuthUtils.getCurrentId()).thenReturn(1);
        when(agencyPropertyRepository.findById(10)).thenReturn(Optional.of(agencyProperty));

        assertThrows(NotFoundException.class, () -> agencyPropertyService.update(10, dto));
    }
}
