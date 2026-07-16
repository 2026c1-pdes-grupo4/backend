package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.property.PropertyType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AgencyPropertyRequestDTO {

    private Integer propertyId;
    private BigDecimal listedPrice;
    private String imageUrl;
    private PropertyType propertyType;
    private String address;
    private String city;
    private String province;
    private Double price;
    private Double areaSq;
    private Integer rooms;
    private String description;
}
