package ar.edu.unq.backend.favorite;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(target = "id",                source = "favoriteId")
    @Mapping(target = "agencyPropertyId",  source = "agencyProperty.agencyPropertyId")
    @Mapping(target = "propertyAddress",   source = "agencyProperty.property.address")
    @Mapping(target = "city",              source = "agencyProperty.property.city")
    @Mapping(target = "agencyName",        source = "agencyProperty.agency.username")
    @Mapping(target = "score",             source = "score")
    @Mapping(target = "comment",           source = "comment")
    @Mapping(target = "savedPrice",        source = "savedPrice")
    @Mapping(target = "savedDate",         source = "savedDate")
    FavoriteResponseDTO mapToResponse(Favorite favorite);
}
