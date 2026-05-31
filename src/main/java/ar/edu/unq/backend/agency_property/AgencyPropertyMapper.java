package ar.edu.unq.backend.agency_property;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgencyPropertyMapper {

    @Mapping(target = "id", source = "agencyPropertyId")
    @Mapping(target = "propertyId", source = "property.propertyId")
    @Mapping(target = "address", source = "property.address")
    @Mapping(target = "city", source = "property.city")
    @Mapping(target = "propertyType", source = "property.propertyType")
    @Mapping(target = "agencyId", source = "agency.agencyId")
    @Mapping(target = "agencyName", source = "agency.username")
    @Mapping(target = "available", source = "property.available")
    AgencyPropertyResponseDTO mapToResponse(AgencyProperty save);
}
