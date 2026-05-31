package ar.edu.unq.backend.favorite;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class FavoriteResponseDTO {
    private Integer id;
    private Integer agencyPropertyId;
    private String propertyAddress;
    private String city;
    private String agencyName;
    private Integer score;
    private String comment;
    private BigDecimal savedPrice;
    private LocalDate savedDate;
}
