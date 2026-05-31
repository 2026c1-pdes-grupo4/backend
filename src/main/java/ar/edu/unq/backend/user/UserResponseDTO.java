package ar.edu.unq.backend.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private ProfileType profileType;
}
