package ar.edu.unq.backend.favorite;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FavoriteRequestDTO {
    private Integer agencyPropertyId;
    private Integer score; // 0–10
    private String comment;
}
