package ar.edu.unq.backend.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequestDTO {
    private String username;
    private String email;
    private String password;
    private ProfileType profileType;
}
