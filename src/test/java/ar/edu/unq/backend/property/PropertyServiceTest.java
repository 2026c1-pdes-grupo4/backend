package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyMapper;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.agency_property.AgencyPropertyResponseDTO;
import ar.edu.unq.backend.common.dto.PagedResultDTO;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.picture.PictureService;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private AgencyPropertyRepository agencyPropertyRepository;
    @Mock private PropertyMapper propertyMapper;
    @Mock private AgencyPropertyMapper agencyPropertyMapper;
    @Mock private PictureService pictureService;

    @InjectMocks
    private PropertyService propertyService;

    private void stubEmptyPage() {
        when(agencyPropertyRepository.searchActiveListings(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
    }

    private PagedResultDTO<AgencyPropertyResponseDTO> searchWithDefaults(
            String city, String province, String propertyType,
            Integer roomsMin, Integer roomsMax,
            BigDecimal priceMin, BigDecimal priceMax, String keyword) {
        return propertyService.search(city, province, propertyType,
                roomsMin, roomsMax, priceMin, priceMax, keyword, 0, 10);
    }

    @Test
    void searchThrowsWhenPriceRangeIsInvalid() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, null, null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), null));
    }

    @Test
    void searchThrowsWhenPriceMinIsNegative() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, null, null,
                BigDecimal.valueOf(-1), null, null));
    }

    @Test
    void searchThrowsWhenPriceMaxIsNegative() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, null, null,
                null, BigDecimal.valueOf(-500), null));
    }

    @Test
    void searchAcceptsPriceMinEqualToPriceMax() {
        stubEmptyPage();
        assertDoesNotThrow(() -> searchWithDefaults(
                null, null, null, null, null,
                BigDecimal.valueOf(100000), BigDecimal.valueOf(100000), null));
    }

    @Test
    void searchThrowsWhenRoomsMinIsZero() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, 0, null, null, null, null));
    }

    @Test
    void searchThrowsWhenRoomsMinIsNegative() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, -1, null, null, null, null));
    }

    @Test
    void searchThrowsWhenRoomsMaxIsZero() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, null, 0, null, null, null));
    }

    @Test
    void searchThrowsWhenRoomsMaxIsNegative() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, null, -2, null, null, null));
    }

    @Test
    void searchThrowsWhenRoomsMinGreaterThanRoomsMax() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, null, 5, 3, null, null, null));
    }

    @Test
    void searchThrowsWhenPageSizeIsZero() {
        assertThrows(ValidationException.class, () ->
                propertyService.search(null, null, null, null, null, null, null, null, 0, 0));
    }

    @Test
    void searchThrowsWhenPageSizeExceedsMaximum() {
        assertThrows(ValidationException.class, () ->
                propertyService.search(null, null, null, null, null, null, null, null, 0, 101));
    }

    @Test
    void searchThrowsWhenPageIsNegative() {
        assertThrows(ValidationException.class, () ->
                propertyService.search(null, null, null, null, null, null, null, null, -1, 10));
    }

    @Test
    void searchThrowsWhenPropertyTypeInvalid() {
        assertThrows(ValidationException.class, () -> searchWithDefaults(
                null, null, "LOT", null, null, null, null, null));
    }

    @Test
    void searchTreatsEmptyStringCityAsNoFilter() {
        when(agencyPropertyRepository.searchActiveListings(
                isNull(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        searchWithDefaults("", null, null, null, null, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                isNull(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchTreatsBlankKeywordAsNoFilter() {
        when(agencyPropertyRepository.searchActiveListings(
                any(), any(), any(), any(), any(), any(), any(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        searchWithDefaults(null, null, null, null, null, null, null, "   ");

        verify(agencyPropertyRepository).searchActiveListings(
                any(), any(), any(), any(), any(), any(), any(), isNull(), any());
    }

    @Test
    void searchTrimsWhitespacesFromCity() {
        when(agencyPropertyRepository.searchActiveListings(
                eq("Buenos Aires"), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        searchWithDefaults("  Buenos Aires  ", null, null, null, null, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                eq("Buenos Aires"), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchTrimsAllStringFilters() {
        when(agencyPropertyRepository.searchActiveListings(
                eq("Buenos Aires"), eq("Buenos Aires"), any(), any(), any(), any(), any(), eq("luminoso"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        searchWithDefaults("  Buenos Aires  ", "  Buenos Aires  ", null, null, null, null, null, "  luminoso  ");

        verify(agencyPropertyRepository).searchActiveListings(
                eq("Buenos Aires"), eq("Buenos Aires"), any(), any(), any(), any(), any(), eq("luminoso"), any());
    }

    @Test
    void searchReturnsPaginatedResult() {
        AgencyProperty ap = new AgencyProperty();
        ap.setAgencyPropertyId(1);
        AgencyPropertyResponseDTO dto = new AgencyPropertyResponseDTO();

        when(agencyPropertyRepository.searchActiveListings(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(ap), PageRequest.of(0, 10), 25));
        when(agencyPropertyMapper.mapToResponse(ap)).thenReturn(dto);
        when(pictureService.firstPictureUrl(1)).thenReturn(null);

        PagedResultDTO<AgencyPropertyResponseDTO> result =
                propertyService.search(null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(25, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void searchReturnsEmptyPageWhenNoResults() {
        when(agencyPropertyRepository.searchActiveListings(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        PagedResultDTO<AgencyPropertyResponseDTO> result =
                searchWithDefaults("CiudadQueNoExiste", null, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void searchPassesAllFiltersToRepository() {
        stubEmptyPage();

        propertyService.search("Buenos Aires", "Buenos Aires", "APARTMENT",
                2, 4, BigDecimal.valueOf(100000), BigDecimal.valueOf(500000), "luminoso", 0, 10);

        verify(agencyPropertyRepository).searchActiveListings(
                eq("Buenos Aires"), eq("Buenos Aires"), eq(PropertyType.APARTMENT),
                eq(2), eq(4),
                eq(100000.0), eq(500000.0), eq("luminoso"),
                eq(PageRequest.of(0, 10)));
    }

    @Test
    void searchWithRoomsRangePassesBothBoundsToRepository() {
        stubEmptyPage();

        searchWithDefaults(null, null, null, 2, 5, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                isNull(), isNull(), isNull(), eq(2), eq(5), isNull(), isNull(), isNull(), any());
    }

    @Test
    void searchWithOnlyRoomsMinPassesNullForRoomsMax() {
        stubEmptyPage();

        searchWithDefaults(null, null, null, 3, null, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                any(), any(), any(), eq(3), isNull(), any(), any(), any(), any());
    }

    @Test
    void searchNormalizesBlankCityButPassesValidProvinceThrough() {
        when(agencyPropertyRepository.searchActiveListings(
                isNull(), eq("Córdoba"), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        searchWithDefaults("   ", "Córdoba", null, null, null, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                isNull(), eq("Córdoba"), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchThrowsForNegativePriceMinEvenWhenOtherFiltersAreValid() {
        assertThrows(ValidationException.class, () -> propertyService.search(
                "Buenos Aires", "Buenos Aires", "HOUSE",
                2, 4, BigDecimal.valueOf(-1), BigDecimal.valueOf(500000), "casa", 0, 10));
    }

    @Test
    void searchThrowsForInvalidRoomsMinEvenWhenPriceRangeIsValid() {
        assertThrows(ValidationException.class, () -> propertyService.search(
                "Córdoba", null, "APARTMENT",
                0, null, BigDecimal.valueOf(50000), BigDecimal.valueOf(200000), null, 0, 10));
    }

    @Test
    void searchWithAllFiltersNullReturnsAllActiveListings() {
        AgencyProperty ap = new AgencyProperty();
        ap.setAgencyPropertyId(1);
        AgencyPropertyResponseDTO dto = new AgencyPropertyResponseDTO();

        when(agencyPropertyRepository.searchActiveListings(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(ap)));
        when(agencyPropertyMapper.mapToResponse(ap)).thenReturn(dto);
        when(pictureService.firstPictureUrl(1)).thenReturn(null);

        PagedResultDTO<AgencyPropertyResponseDTO> result =
                searchWithDefaults(null, null, null, null, null, null, null, null);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchWithPropertyTypeAndRoomsMinPassesParsedTypeToRepository() {
        stubEmptyPage();

        searchWithDefaults(null, null, "house", 5, null, null, null, null);

        verify(agencyPropertyRepository).searchActiveListings(
                isNull(), isNull(), eq(PropertyType.HOUSE), eq(5), isNull(),
                isNull(), isNull(), isNull(), any());
    }

    @Test
    void searchMapsAgencyDataIntoResponse() {
        Property property = new Property();
        property.setPropertyId(10);
        property.setAvailable(true);

        Agency agency = new Agency();
        agency.setAgencyId(7);
        agency.setUsername("inmo");

        AgencyProperty ap = new AgencyProperty();
        ap.setAgencyPropertyId(99);
        ap.setProperty(property);
        ap.setAgency(agency);
        ap.setListedPrice(200000.0);

        AgencyPropertyResponseDTO base = new AgencyPropertyResponseDTO();
        base.setAgencyId(7);
        base.setAgencyName("inmo");
        base.setListedPrice(200000.0);

        when(agencyPropertyRepository.searchActiveListings(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(ap)));
        when(agencyPropertyMapper.mapToResponse(ap)).thenReturn(base);
        when(pictureService.firstPictureUrl(99)).thenReturn("https://images.unsplash.com/photo-1");

        PagedResultDTO<AgencyPropertyResponseDTO> result =
                searchWithDefaults("Buenos Aires", null, null, null, null, null, null, "A");

        assertEquals(1, result.getContent().size());
        assertEquals(7, result.getContent().get(0).getAgencyId());
        assertEquals("inmo", result.getContent().get(0).getAgencyName());
        assertEquals(200000.0, result.getContent().get(0).getListedPrice());
        assertEquals("https://images.unsplash.com/photo-1", result.getContent().get(0).getImageUrl());
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
    void findByIdMapsEntityWhenExists() {
        Property entity = new Property();
        when(propertyRepository.findById(5)).thenReturn(java.util.Optional.of(entity));
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());
        assertNotNull(propertyService.findById(5));
    }

    @Test
    void findAllMapsEntities() {
        Property entity = new Property();
        when(propertyRepository.findAll()).thenReturn(List.of(entity));
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());
        assertEquals(1, propertyService.findAll().size());
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
    void deleteCallsRepositoryWhenFound() {
        Property entity = new Property();
        entity.setAvailable(true);
        when(propertyRepository.findById(1)).thenReturn(java.util.Optional.of(entity));
        when(agencyPropertyRepository.existsByProperty_PropertyIdAndDeletedFalse(1)).thenReturn(false);

        propertyService.delete(1);

        verify(propertyRepository).delete(entity);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(propertyRepository.findById(77)).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class, () -> propertyService.delete(77));
    }

    @Test
    void deleteThrowsWhenPropertyIsSold() {
        Property entity = new Property();
        entity.setAvailable(false);
        when(propertyRepository.findById(2)).thenReturn(java.util.Optional.of(entity));

        assertThrows(ValidationException.class, () -> propertyService.delete(2));
        verify(propertyRepository, never()).delete(any(Property.class));
    }

    @Test
    void deleteThrowsWhenPropertyHasActivePublications() {
        Property entity = new Property();
        entity.setAvailable(true);
        when(propertyRepository.findById(3)).thenReturn(java.util.Optional.of(entity));
        when(agencyPropertyRepository.existsByProperty_PropertyIdAndDeletedFalse(3)).thenReturn(true);

        assertThrows(ValidationException.class, () -> propertyService.delete(3));
        verify(propertyRepository, never()).delete(any(Property.class));
    }

    @Test
    void findByCadastralReturnsMappedDtoWhenFound() {
        Property entity = new Property();
        when(propertyRepository.findByCircumscriptionAndSectionAndBlockAndParcel("1", "A", "10", "5"))
                .thenReturn(java.util.Optional.of(entity));
        when(propertyMapper.toResponse(entity)).thenReturn(new PropertyResponseDTO());

        assertNotNull(propertyService.findByCadastral("1", "A", "10", "5"));
    }

    @Test
    void findByCadastralThrowsWhenNoMatch() {
        when(propertyRepository.findByCircumscriptionAndSectionAndBlockAndParcel("1", "A", "10", "5"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> propertyService.findByCadastral("1", "A", "10", "5"));
    }
}

