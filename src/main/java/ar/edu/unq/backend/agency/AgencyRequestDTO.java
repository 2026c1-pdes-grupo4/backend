package ar.edu.unq.backend.agency;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AgencyRequestDTO {
    private String username;
    private String email;
    private String password;

}
