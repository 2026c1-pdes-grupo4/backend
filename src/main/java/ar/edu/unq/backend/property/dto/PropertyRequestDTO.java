package ar.edu.unq.backend.property.dto;

import ar.edu.unq.backend.property.PropertyType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRequestDTO {

    private PropertyType propertyType;
    private String address;
    private String city;
    private String province;
    private Double areaSq;
    private Integer rooms;
    private String description;
    private String circumscription;
    private String section;
    private String block;
    private String parcel;
}
