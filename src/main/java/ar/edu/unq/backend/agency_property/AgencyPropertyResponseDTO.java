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
    private String propertyType;
    private Double listedPrice;
    private LocalDate listedDate;
    private Boolean available;
    private Integer agencyId;
    private String agencyName;
}
