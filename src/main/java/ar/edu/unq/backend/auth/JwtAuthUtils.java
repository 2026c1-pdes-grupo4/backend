package ar.edu.unq.backend.auth;

import io.jsonwebtoken.Claims;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class JwtAuthUtils {

    private Claims getClaims() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (Claims) authentication.getDetails();
    }

    public Integer getCurrentId() {
        return getClaims().get("id", Integer.class);
    }

    public String getCurrentType() {
        return getClaims().get("typ", String.class);
    }

    public String getCurrentUsername() {
        return getClaims().getSubject();
    }

}
