package ar.edu.unq.backend.purchase;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PurchaseResponseDTO {
    private Integer id;
    private String propertyAddress;
    private String agencyName;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
}
