package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyMapper;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.agency_property.AgencyPropertyResponseDTO;
import ar.edu.unq.backend.agency_property.AgencyPropertySpecification;
import ar.edu.unq.backend.common.dto.PagedResultDTO;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.picture.PictureService;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * Servicio que gestiona el ciclo de vida de las propiedades inmobiliarias.
 */
@Service
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;
    private final AgencyPropertyRepository agencyPropertyRepository;
    private final PropertyMapper propertyMapper;
    private final AgencyPropertyMapper agencyPropertyMapper;
    private final PictureService pictureService;

    public PropertyService(PropertyRepository propertyRepository,
                           AgencyPropertyRepository agencyPropertyRepository,
                           PropertyMapper propertyMapper,
                           AgencyPropertyMapper agencyPropertyMapper,
                           PictureService pictureService) {
        this.propertyRepository = propertyRepository;
        this.agencyPropertyRepository = agencyPropertyRepository;
        this.propertyMapper = propertyMapper;
        this.agencyPropertyMapper = agencyPropertyMapper;
        this.pictureService = pictureService;
    }

    /**
     * Devuelve la lista de todas las propiedades registradas.
     *
     * @return lista de propiedades como DTOs de respuesta
     */
    public List<PropertyResponseDTO> findAll() {
        log.info("Fetching all properties.");
        List<PropertyResponseDTO> result = propertyRepository.findAll()
                        .stream()
                        .map(propertyMapper::toResponse)
                        .toList();
        log.info("Properties fetched. count={}", result.size());
        return result;
    }

    /**
     * Busca una propiedad por el id.
     *
     * @param id identificador de la propiedad
     * @return la propiedad encontrada como DTO de respuesta
     * @throws RuntimeException si la propiedad no existe
     */
    public PropertyResponseDTO findById(Integer id) {
        log.info("Fetching property.");
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Property not found");
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });
        log.info("Property found.");
        return propertyMapper.toResponse(property);
    }

    /**
     * Crea y persiste una nueva propiedad.
     *
     * @param dto datos de la propiedad a crear
     * @return la propiedad creada como DTO de respuesta
     */
    @Transactional
    public PropertyResponseDTO create(PropertyRequestDTO dto) {
        log.info("Property: {}", dto.toString());
        Property p = propertyMapper.toEntity(dto);
        p.setAvailable(true);

        PropertyResponseDTO result = propertyMapper.toResponse(propertyRepository.save(p));
        log.info("Property created.");
        return result;
    }

    /**
     * Actualiza los datos de una propiedad existente.
     *
     * @param id  identificador de la propiedad a actualizar
     * @param dto nuevos datos de la propiedad
     * @return la propiedad actualizada como DTO de respuesta
     * @throws RuntimeException si la propiedad no existe.
     */
    @Transactional
    public PropertyResponseDTO update(Integer id, PropertyRequestDTO dto) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update property because it does not exist.");
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });

        propertyMapper.updateEntity(dto, existing);

        PropertyResponseDTO result = propertyMapper.toResponse(propertyRepository.save(existing));
        log.info("Property updated.");
        return result;
    }

    /**
     * Elimina una propiedad por el id.
     * No se puede eliminar una propiedad que ya fue vendida ni una que tenga publicaciones activas.
     *
     * @param id identificador de la propiedad a eliminar
     * @throws ValidationException si la propiedad fue vendida o tiene publicaciones asociadas
     */
    @Transactional
    public void delete(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete property because it does not exist.");
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });

        if (!property.getAvailable()) {
            log.error("Cannot delete property: it has already been sold. propertyId={}", id);
            throw new ValidationException(ErrorCode.SOLD_PROPERTY_CANNOT_BE_DELETED, "Cannot delete a sold property");
        }

        if (agencyPropertyRepository.existsByProperty_PropertyIdAndDeletedFalse(id)) {
            log.error("Cannot delete property: it has active publications. propertyId={}", id);
            throw new ValidationException(ErrorCode.PROPERTY_HAS_ACTIVE_PUBLICATIONS, "Cannot delete a property with active publications");
        }

        propertyRepository.delete(property);
        log.info("Property deleted. propertyId={}", id);
    }

    public PagedResultDTO<AgencyPropertyResponseDTO> search(
            String city, String province, String propertyType,
            Integer roomsMin, Integer roomsMax,
            BigDecimal priceMin, BigDecimal priceMax,
            String keyword, int page, int size) {

        city     = normalizeString(city);
        province = normalizeString(province);
        keyword  = normalizeString(keyword);

        if (priceMin != null && priceMin.signum() < 0) {
            log.error("Rejecting property search: priceMin is negative.");
            throw new ValidationException(ErrorCode.INVALID_PRICE_RANGE,
                    "priceMin must be >= 0", List.of("priceMin must be >= 0"));
        }
        if (priceMax != null && priceMax.signum() < 0) {
            log.error("Rejecting property search: priceMax is negative.");
            throw new ValidationException(ErrorCode.INVALID_PRICE_RANGE,
                    "priceMax must be >= 0", List.of("priceMax must be >= 0"));
        }
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            log.error("Rejecting property search: invalid price range.");
            throw new ValidationException(ErrorCode.INVALID_PRICE_RANGE,
                    "priceMin must be less than or equal to priceMax", List.of("priceMin > priceMax"));
        }
        if (roomsMin != null && roomsMin <= 0) {
            log.error("Rejecting property search: roomsMin must be > 0.");
            throw new ValidationException(ErrorCode.INVALID_ROOMS,
                    "roomsMin must be greater than zero", List.of("roomsMin must be > 0"));
        }
        if (roomsMax != null && roomsMax <= 0) {
            log.error("Rejecting property search: roomsMax must be > 0.");
            throw new ValidationException(ErrorCode.INVALID_ROOMS,
                    "roomsMax must be greater than zero", List.of("roomsMax must be > 0"));
        }
        if (roomsMin != null && roomsMax != null && roomsMin > roomsMax) {
            log.error("Rejecting property search: roomsMin > roomsMax.");
            throw new ValidationException(ErrorCode.INVALID_ROOMS,
                    "roomsMin must be less than or equal to roomsMax", List.of("roomsMin > roomsMax"));
        }
        if (size <= 0 || size > 100) {
            log.error("Rejecting property search: invalid page size={}.", size);
            throw new ValidationException(ErrorCode.INVALID_REQUEST,
                    "size must be between 1 and 100", List.of("size must be between 1 and 100"));
        }
        if (page < 0) {
            log.error("Rejecting property search: negative page={}.", page);
            throw new ValidationException(ErrorCode.INVALID_REQUEST,
                    "page must be >= 0", List.of("page must be >= 0"));
        }

        PropertyType parsedType = parsePropertyType(propertyType);

        Specification<AgencyProperty> spec =
                AgencyPropertySpecification.searchActiveListings(
                        city, province, parsedType,
                        roomsMin, roomsMax,
                        priceMin == null ? null : priceMin.doubleValue(),
                        priceMax == null ? null : priceMax.doubleValue(),
                        keyword
                );

        Page<AgencyProperty> resultPage = agencyPropertyRepository.findAll(spec, PageRequest.of(page, size));

        List<AgencyPropertyResponseDTO> content = resultPage.getContent()
                .stream()
                .map(this::toSearchResponse)
                .toList();

        log.info("Property search completed. page={}, size={}, total={}", page, size, resultPage.getTotalElements());
        return new PagedResultDTO<>(content, page, size, resultPage.getTotalElements(), resultPage.getTotalPages());
    }

    private String normalizeString(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    /**
     * Busca una propiedad existente por sus datos catastrales.
     * Se usa para detectar duplicados antes de crear una propiedad nueva.
     *
     * @return la propiedad encontrada como DTO de respuesta
     * @throws RuntimeException si no existe una propiedad con esos datos catastrales
     */
    public PropertyResponseDTO findByCadastral(String circumscription, String section, String block, String parcel) {
        log.info("Looking up property by cadastral data.");
        Property property = propertyRepository.findByCircumscriptionAndSectionAndBlockAndParcel(circumscription, section, block, parcel)
                .orElseThrow(() -> {
                    log.error("No property found for given cadastral data.");
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });
        log.info("Property found by cadastral data.");
        return propertyMapper.toResponse(property);
    }

    private PropertyType parsePropertyType(String propertyType) {
        if (propertyType == null || propertyType.isBlank()) {
            return null;
        }

        try {
            return PropertyType.valueOf(propertyType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.error("Rejecting property search: invalid propertyType.");
            throw new ValidationException(
                    ErrorCode.INVALID_PROPERTY_TYPE,
                    "Invalid propertyType",
                    List.of("propertyType must be HOUSE or APARTMENT")
            );
        }
    }

    private AgencyPropertyResponseDTO toSearchResponse(AgencyProperty listing) {
        AgencyPropertyResponseDTO dto = agencyPropertyMapper.mapToResponse(listing);
        dto.setImageUrl(pictureService.firstPictureUrl(listing.getAgencyPropertyId()));
        return dto;
    }
}