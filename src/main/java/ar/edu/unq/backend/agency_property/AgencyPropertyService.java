package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.agency.Agency;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.auth.JwtAuthUtils;
import ar.edu.unq.backend.property.Property;
import ar.edu.unq.backend.property.PropertyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que gestiona las publicaciones de propiedades por parte de las agencias.
 */
@Service
public class AgencyPropertyService {

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
     * @throws ResponseStatusException 404 si la agencia o la propiedad no existen,
     *                                 400 si la propiedad ya fue publicada por esta agencia
     */
    public AgencyPropertyResponseDTO publish(AgencyPropertyRequestDTO dto) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency not found"));

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (agencyPropertyRepository.existsByAgency_AgencyIdAndProperty_PropertyId(agencyId, property.getPropertyId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property already published by this agency");
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
     * @throws ResponseStatusException 404 si la publicación no existe
     */
    public AgencyPropertyResponseDTO findById(Integer id) {
        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agency property not found"));

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
     * @throws ResponseStatusException 404 si la publicación no existe,
     *                                 403 si la agencia autenticada no es la propietaria
     */
    public AgencyPropertyResponseDTO updatePrice(Integer id, AgencyPropertyRequestDTO dto) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency property not found"));

        if (!ap.getAgency().getAgencyId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another agency publication");
        }

        ap.setListedPrice(dto.getListedPrice().doubleValue());

        return agencyPropertyMapper.mapToResponse(agencyPropertyRepository.save(ap));
    }

    /**
     * Elimina una publicación por el id.
     * Solo la agencia propietaria puede eliminarla y únicamente si la propiedad no fue vendida.
     *
     * @param id identificador de la publicación a eliminar
     * @throws ResponseStatusException 404 si la publicación no existe,
     *                                 403 si la agencia autenticada no es la propietaria,
     *                                 400 si la propiedad ya fue vendida
     */
    public void delete(Integer id) {
        Integer agencyId = jwtAuthUtils.getCurrentId();

        AgencyProperty ap = agencyPropertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agency property not found"));

        if (!ap.getAgency().getAgencyId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another agency publication");
        }

        if (!ap.getProperty().getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a sold publication");
        }

        agencyPropertyRepository.delete(ap);
    }
}
