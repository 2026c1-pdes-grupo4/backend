package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.common.error.ErrorCode;
import ar.edu.unq.backend.common.exception.ConflictException;
import ar.edu.unq.backend.common.exception.ForbiddenException;
import ar.edu.unq.backend.common.exception.NotFoundException;
import ar.edu.unq.backend.common.exception.ValidationException;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona las publicaciones de propiedades por parte de las agencias.
 */
@Service
public class AgencyPropertyService {

    private static final Logger log = LoggerFactory.getLogger(AgencyPropertyService.class);

    private final AgencyPropertyRepository agencyPropertyRepository;
    private final PropertyRepository propertyRepository;
    private final AgencyRepository agencyRepository;
    private final AgencyPropertyMapper agencyPropertyMapper;
    private final JwtAuthUtils jwtAuthUtils;

    public AgencyPropertyService(AgencyPropertyRepository agencyPropertyRepository, PropertyRepository propertyRepository,
                                 AgencyRepository agencyRepository, AgencyPropertyMapper agencyPropertyMapper, JwtAuthUtils jwtAuthUtils) {
        this.agencyPropertyRepository = agencyPropertyRepository;
        this.propertyRepository = propertyRepository;
        this.agencyRepository = agencyRepository;
        this.agencyPropertyMapper = agencyPropertyMapper;
        this.jwtAuthUtils = jwtAuthUtils;
    }

    /**
     * Publica una propiedad asociándola a la agencia autenticada.
     * Registra automáticamente la fecha de publicación.
     *
     * @param dto datos de la publicación, incluyendo el id de la propiedad y el precio
     * @return la publicación creada como DTO de respuesta
     * @throws RuntimeException si la agencia o la propiedad no existen,
     *                                 400 si la propiedad ya fue publicada por esta agencia
     */
    public AgencyPropertyResponseDTO publish(AgencyPropertyRequestDTO dto) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> {
                    log.error("Cannot publish property: agency not found. agencyId={}", agencyId);
                    return new NotFoundException(ErrorCode.AGENCY_NOT_FOUND, "Agency not found");
                });

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> {
                    log.error("Cannot publish property: property not found. propertyId={}", dto.getPropertyId());
                    return new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND, "Property not found");
                });

        validateListedPrice(dto);

        if (agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyId(agencyId, property.getPropertyId())) {
            log.error("Rejecting publication: already published by agency. agencyId={}, propertyId={}", agencyId, property.getPropertyId());
            throw new ConflictException(ErrorCode.PUBLICATION_ALREADY_EXISTS_FOR_AGENCY, "Property already published by this agency");
        }

        AgencyProperty ap = new AgencyProperty();
        ap.setAgency(agency);
        ap.setProperty(property);
        ap.setListedPrice(dto.getListedPrice().doubleValue());
        ap.setListedDate(LocalDate.now());
        ap.getProperty().setAvailable(true);

        return agencyPropertyMapper.mapToResponse(agencyPropertyRepository.save(ap));
    }

    /**
     * Busca una publicación por el id.
     *
     * @param id identificador de la publicación
     * @return la publicación encontrada como DTO de respuesta
     * @throws RuntimeException si la publicación no existe
     */
    public AgencyPropertyResponseDTO findById(Integer id) {
        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Agency publication not found. agencyPropertyId={}", id);
                    return new NotFoundException(
                            ErrorCode.AGENCY_PROPERTY_NOT_FOUND, "Agency property not found");
                });

        return agencyPropertyMapper.mapToResponse(ap);
    }

    /**
     * Devuelve todas las publicaciones pertenecientes a la agencia autenticada.
     *
     * @return lista de publicaciones de la agencia como DTOs de respuesta
     */
    public List<AgencyPropertyResponseDTO> findByCurrentAgency() {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        return agencyPropertyRepository.findByAgency_AgencyId(agencyId)
                .stream()
                .map(agencyPropertyMapper::mapToResponse)
                .toList();
    }

    /**
     * Actualiza el precio de una publicación existente.
     * Solo la agencia propietaria puede modificarla.
     *
     * @param id  identificador de la publicación
     * @param dto datos con el nuevo precio
     * @return la publicación actualizada como DTO de respuesta
     * @throws RuntimeException si la publicación no existe,
     *                                 403 si la agencia autenticada no es la propietaria
     */
    public AgencyPropertyResponseDTO updatePrice(Integer id, AgencyPropertyRequestDTO dto) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update publication price: publication not found. agencyPropertyId={}", id);
                    return new NotFoundException(ErrorCode.AGENCY_PROPERTY_NOT_FOUND, "Agency property not found");
                });

        if (!ap.getAgency().getAgencyId().equals(agencyId)) {
            log.error("Rejecting publication price update: ownership mismatch. agencyPropertyId={}, requesterAgencyId={}, ownerAgencyId={}",
                    id, agencyId, ap.getAgency().getAgencyId());
            throw new ForbiddenException(ErrorCode.CANNOT_MODIFY_OTHER_PUBLICATION, "Cannot modify another agency publication");
        }

        validateListedPrice(dto);

        ap.setListedPrice(dto.getListedPrice().doubleValue());

        return agencyPropertyMapper.mapToResponse(agencyPropertyRepository.save(ap));
    }

    /**
     * Elimina una publicación por el id.
     * Solo la agencia propietaria puede eliminarla y únicamente si la propiedad no fue vendida.
     *
     * @param id identificador de la publicación a eliminar
     * @throws RuntimeException si la publicación no existe,
     *                                 403 si la agencia autenticada no es la propietaria,
     *                                 400 si la propiedad ya fue vendida
     */
    public void delete(Integer id) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot delete publication: publication not found. agencyPropertyId={}", id);
                    return new NotFoundException(ErrorCode.AGENCY_PROPERTY_NOT_FOUND, "Agency property not found");
                });

        if (!ap.getAgency().getAgencyId().equals(agencyId)) {
            log.error("Rejecting publication delete: ownership mismatch. agencyPropertyId={}, requesterAgencyId={}, ownerAgencyId={}",
                    id, agencyId, ap.getAgency().getAgencyId());
            throw new ForbiddenException(ErrorCode.CANNOT_DELETE_OTHER_PUBLICATION, "Cannot delete another agency publication");
        }

        if (!ap.getProperty().getAvailable()) {
            log.error("Rejecting publication delete: publication already sold. agencyPropertyId={}", id);
            throw new ValidationException(ErrorCode.SOLD_PUBLICATION_CANNOT_BE_DELETED, "Cannot delete a sold publication");
        }

        agencyPropertyRepository.delete(ap);
    }

    private void validateListedPrice(AgencyPropertyRequestDTO dto) {
        if (dto.getListedPrice() == null || dto.getListedPrice().doubleValue() <= 0) {
            log.error("Rejecting publication operation: listedPrice must be > 0. listedPrice={}", dto.getListedPrice());
            throw new ValidationException(
                    ErrorCode.INVALID_REQUEST,
                    "listedPrice must be greater than zero",
                    List.of("listedPrice must be > 0")
            );
        }
    }
}
