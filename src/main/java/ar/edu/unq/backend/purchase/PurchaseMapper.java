package ar.edu.unq.backend.purchase;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    @Mapping(target = "id",              source = "purchaseId")
    @Mapping(target = "propertyAddress", source = "agencyProperty.property.address")
    @Mapping(target = "agencyName",      source = "agencyProperty.agency.username")
    @Mapping(target = "purchasePrice",   source = "purchasePrice")
    @Mapping(target = "purchaseDate",    source = "purchaseDate")
    @Mapping(target = "buyerId",         source = "user.userId")
    @Mapping(target = "buyerUsername",   source = "user.username")
    @Mapping(target = "buyerEmail",      source = "user.email")
    PurchaseResponseDTO mapToResponse(Purchase purchase);
}
