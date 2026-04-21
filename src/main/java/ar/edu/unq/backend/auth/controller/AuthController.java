package ar.edu.unq.backend.auth.controller;

import ar.edu.unq.backend.auth.dto.LoginRequest;
import ar.edu.unq.backend.auth.dto.LoginResponse;
import ar.edu.unq.backend.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}