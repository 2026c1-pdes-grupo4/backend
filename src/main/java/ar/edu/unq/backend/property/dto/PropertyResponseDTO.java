package ar.edu.unq.backend.property.dto;

import ar.edu.unq.backend.property.PropertyType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyResponseDTO {

    private Integer propertyId;
    private PropertyType propertyType;
    private Double price;
    private String address;
    private String city;
    private String province;
    private Double areaSq;
    private Integer rooms;
    private String description;
    private Boolean available;

}

