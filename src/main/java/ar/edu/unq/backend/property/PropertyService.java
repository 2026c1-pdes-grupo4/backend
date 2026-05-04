package ar.edu.unq.backend.property;

import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio que gestiona el ciclo de vida de las propiedades inmobiliarias.
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyService(PropertyRepository propertyRepository, PropertyMapper propertyMapper) {
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
    }

    /**
     * Devuelve la lista de todas las propiedades registradas.
     *
     * @return lista de propiedades como DTOs de respuesta
     */
    public List<PropertyResponseDTO> findAll() {
        return propertyRepository.findAll()
                        .stream()
                        .map(propertyMapper::toResponse)
                        .toList();
    }

    /**
     * Busca una propiedad por el id.
     *
     * @param id identificador de la propiedad
     * @return la propiedad encontrada como DTO de respuesta
     * @throws ResponseStatusException 404 si la propiedad no existe
     */
    public PropertyResponseDTO findById(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

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

        return propertyMapper.toResponse(propertyRepository.save(p));
    }

    /**
     * Actualiza los datos de una propiedad existente.
     *
     * @param id  identificador de la propiedad a actualizar
     * @param dto nuevos datos de la propiedad
     * @return la propiedad actualizada como DTO de respuesta
     * @throws ResponseStatusException 404 si la propiedad no existe.
     */
    @Transactional
    public PropertyResponseDTO update(Integer id, PropertyRequestDTO dto) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        propertyMapper.updateEntity(dto, existing);

        return propertyMapper.toResponse(propertyRepository.save(existing));
    }

    /**
     * Elimina una propiedad por el id.
     *
     * @param id identificador de la propiedad a eliminar
     */
    @Transactional
    public void delete(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        propertyRepository.delete(property);
    }

    // TODO: implementar búsqueda avanzada con filtros opcionales
    public List<PropertyResponseDTO> search(String city, String province, String propertyType, Integer rooms,
            BigDecimal priceMin, BigDecimal priceMax, String keyword) {
        return List.of();
    }
}