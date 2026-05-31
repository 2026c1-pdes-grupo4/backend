package ar.edu.unq.backend.agency_property;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AgencyPropertyRequestDTO {

    private Integer propertyId;
    private BigDecimal listedPrice;

}
