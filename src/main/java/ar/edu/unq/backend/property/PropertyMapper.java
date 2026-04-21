package ar.edu.unq.backend.property;

import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    PropertyResponseDTO toResponse(Property property);

    List<PropertyResponseDTO> toResponseList(List<Property> properties);

    @Mapping(target = "propertyId", ignore = true)
    Property toEntity(PropertyRequestDTO dto);

    @Mapping(target = "propertyId", ignore = true)
    void updateEntity(PropertyRequestDTO dto, @MappingTarget Property property);

}

