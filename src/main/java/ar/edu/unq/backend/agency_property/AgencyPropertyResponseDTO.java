package ar.edu.unq.backend.agency_property;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class AgencyPropertyResponseDTO {
    private Integer id;
    private Integer propertyId;
    private String address;
    private String city;
    private String province;
    private String propertyType;
    private Double areaSq;
    private Integer rooms;
    private String description;
    private String circumscription;
    private String section;
    private String block;
    private String parcel;
    private Double listedPrice;
    private LocalDate listedDate;
    private Boolean available;
    private Integer agencyId;
    private String agencyName;
    private String imageUrl;
}
