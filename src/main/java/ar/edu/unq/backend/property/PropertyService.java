package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency_property.AgencyProperty;
import ar.edu.unq.backend.agency_property.AgencyPropertyRepository;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Servicio que gestiona el ciclo de vida de las propiedades inmobiliarias.
 */
@Service
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;
    private final AgencyPropertyRepository agencyPropertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyService(PropertyRepository propertyRepository,
                           AgencyPropertyRepository agencyPropertyRepository,
                           PropertyMapper propertyMapper) {
        this.propertyRepository = propertyRepository;
        this.agencyPropertyRepository = agencyPropertyRepository;
        this.propertyMapper = propertyMapper;
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
     *
     * @param id identificador de la propiedad a eliminar
     */
    @Transactional
    public void delete(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete property because it does not exist.");
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });

        propertyRepository.delete(property);
        log.info("Property deleted. propertyId={}", id);
    }

    public List<PropertyResponseDTO> search(String city, String province, String propertyType, Integer rooms,
            BigDecimal priceMin, BigDecimal priceMax, String keyword) {
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            log.error("Rejecting property search: invalid price range.");
            throw new ValidationException(
                    ErrorCode.INVALID_PRICE_RANGE,
                    "priceMin must be less than or equal to priceMax",
                    List.of("priceMin > priceMax")
            );
        }

        PropertyType parsedType = parsePropertyType(propertyType);
        List<AgencyProperty> listings = agencyPropertyRepository.searchActiveListings(
                city,
                province,
                parsedType,
                rooms,
                priceMin == null ? null : priceMin.doubleValue(),
                priceMax == null ? null : priceMax.doubleValue(),
                keyword
        );

        List<PropertyResponseDTO> results = listings.stream()
                .map(this::toSearchResponse)
                .toList();

        log.info("Property search completed.");
        return results;
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

    private PropertyResponseDTO toSearchResponse(AgencyProperty listing) {
        PropertyResponseDTO dto = propertyMapper.toResponse(listing.getProperty());
        dto.setListedPrice(BigDecimal.valueOf(listing.getListedPrice()));
        dto.setAgencyId(listing.getAgency().getAgencyId());
        dto.setAgencyName(listing.getAgency().getUsername());
        return dto;
    }
}