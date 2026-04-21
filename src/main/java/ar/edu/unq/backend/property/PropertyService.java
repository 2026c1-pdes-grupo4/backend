package ar.edu.unq.backend.property;

import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyService(PropertyRepository propertyRepository, PropertyMapper propertyMapper) {
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
    }

    public List<PropertyResponseDTO> findAll() {
        return propertyMapper.toResponseList(propertyRepository.findAll());
    }

    public PropertyResponseDTO findById(Integer id) {
        return propertyMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public PropertyResponseDTO create(PropertyRequestDTO dto) {
        validate(dto);
        Property p = propertyMapper.toEntity(dto);
        if (p.getAvailable() == null) p.setAvailable(true);
        return propertyMapper.toResponse(propertyRepository.save(p));
    }

    @Transactional
    public PropertyResponseDTO update(Integer id, PropertyRequestDTO dto) {
        validate(dto);
        Property existing = findEntityById(id);
        propertyMapper.updateEntity(dto, existing);
        return propertyMapper.toResponse(propertyRepository.save(existing));
    }

    @Transactional
    public void delete(Integer id) {
        propertyRepository.deleteById(id);
    }

    private Property findEntityById(Integer id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada: " + id));
    }

    private void validate(PropertyRequestDTO p) {
        if (p.getPropertyType() == null) throw new RuntimeException("propertyType es requerido");
        if (p.getPrice() != null && p.getPrice() < 0) throw new RuntimeException("price debe ser >= 0");
        if (p.getRooms() != null && p.getRooms() < 0) throw new RuntimeException("rooms deben ser >= 0");
        if (p.getAreaSq() != null && p.getAreaSq() < 0) throw new RuntimeException("areaSq debe ser >= 0");
    }
}