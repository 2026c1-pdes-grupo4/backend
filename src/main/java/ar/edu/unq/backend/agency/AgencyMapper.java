package ar.edu.unq.backend.agency;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgencyMapper {

    @Mapping(target = "id",       source = "agencyId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email",    source = "email")
    AgencyResponseDTO mapToResponse(Agency agency);
}
