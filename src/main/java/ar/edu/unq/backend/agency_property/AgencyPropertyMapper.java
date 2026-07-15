package ar.edu.unq.backend.agency_property;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgencyPropertyMapper {

    @Mapping(target = "id", source = "agencyPropertyId")
    @Mapping(target = "propertyId", source = "property.propertyId")
    @Mapping(target = "address", source = "property.address")
    @Mapping(target = "city", source = "property.city")
    @Mapping(target = "province", source = "property.province")
    @Mapping(target = "propertyType", source = "property.propertyType")
    @Mapping(target = "areaSq", source = "property.areaSq")
    @Mapping(target = "rooms", source = "property.rooms")
    @Mapping(target = "description", source = "property.description")
    @Mapping(target = "circumscription", source = "property.circumscription")
    @Mapping(target = "section", source = "property.section")
    @Mapping(target = "block", source = "property.block")
    @Mapping(target = "parcel", source = "property.parcel")
    @Mapping(target = "agencyId", source = "agency.agencyId")
    @Mapping(target = "agencyName", source = "agency.username")
    @Mapping(target = "available", source = "property.available")
    @Mapping(target = "imageUrl", ignore = true)
    AgencyPropertyResponseDTO mapToResponse(AgencyProperty save);
}
