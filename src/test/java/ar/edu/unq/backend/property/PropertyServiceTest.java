package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private AgencyPropertyRepository agencyPropertyRepository;
    @Mock
    private PropertyMapper propertyMapper;

    @InjectMocks
    private PropertyService propertyService;

    @Test
    void searchThrowsWhenPriceRangeIsInvalid() {
        assertThrows(ValidationException.class, () -> propertyService.search(
                null,
                null,
                null,
                null,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                null
        ));
    }

    @Test
    void searchMapsAgencyDataIntoResponse() {
        Property property = new Property();
        property.setPropertyId(10);
        property.setAddress("A");
        property.setAvailable(true);

        Agency agency = new Agency();
        agency.setAgencyId(7);
        agency.setUsername("inmo");

        AgencyProperty ap = new AgencyProperty();
        ap.setProperty(property);
        ap.setAgency(agency);
        ap.setListedPrice(200000.0);

        PropertyResponseDTO base = new PropertyResponseDTO();
        base.setId(10);

        when(agencyPropertyRepository.searchActiveListings(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(ap));
        when(propertyMapper.toResponse(property)).thenReturn(base);

        List<PropertyResponseDTO> result = propertyService.search(
                "Buenos Aires",
                null,
                null,
                null,
                null,
                null,
                "A"
        );

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).getAgencyId());
        assertEquals("inmo", result.get(0).getAgencyName());
        assertEquals(BigDecimal.valueOf(200000.0), result.get(0).getListedPrice());
    }

    @Test
    void searchThrowsWhenPropertyTypeInvalid() {
        assertThrows(ValidationException.class, () -> propertyService.search(
                null,
                null,
                "LOT",
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void createSetsAvailableTrue() {
        PropertyRequestDTO dto = new PropertyRequestDTO();
        Property entity = new Property();
        entity.setAvailable(false);

        when(propertyMapper.toEntity(dto)).thenReturn(entity);
        when(propertyRepository.save(entity)).thenReturn(entity);
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());

        propertyService.create(dto);

        assertTrue(entity.getAvailable());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(propertyRepository.findById(1)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> propertyService.findById(1));
    }

    @Test
    void deleteCallsRepositoryWhenFound() {
        Property entity = new Property();
        when(propertyRepository.findById(1)).thenReturn(java.util.Optional.of(entity));

        propertyService.delete(1);

        verify(propertyRepository).delete(entity);
    }

    @Test
    void findAllMapsEntities() {
        Property entity = new Property();
        when(propertyRepository.findAll()).thenReturn(List.of(entity));
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());

        assertEquals(1, propertyService.findAll().size());
    }

    @Test
    void findByIdMapsEntityWhenExists() {
        Property entity = new Property();
        when(propertyRepository.findById(5)).thenReturn(java.util.Optional.of(entity));
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());

        assertNotNull(propertyService.findById(5));
    }

    @Test
    void updateMapsAndSaves() {
        PropertyRequestDTO dto = new PropertyRequestDTO();
        Property entity = new Property();

        when(propertyRepository.findById(2)).thenReturn(java.util.Optional.of(entity));
        when(propertyRepository.save(entity)).thenReturn(entity);
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());

        propertyService.update(2, dto);

        verify(propertyMapper).updateEntity(dto, entity);
        verify(propertyRepository).save(entity);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(propertyRepository.findById(77)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> propertyService.delete(77));
    }
}

