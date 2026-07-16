package ar.edu.unq.backend.property.dto;

import ar.edu.unq.backend.property.PropertyType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@Setter
public class PropertyRequestDTO {

    private PropertyType propertyType;
    private Double price;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("propertyType", propertyType)
                .append("price", price)
                .append("address", address)
                .append("city", city)
                .append("province", province)
                .append("areaSq", areaSq)
                .append("rooms", rooms)
                .append("description", description)
                .append("circumscription", circumscription)
                .append("section", section)
                .append("block", block)
                .append("parcel", parcel)
                .toString();
    }
}
