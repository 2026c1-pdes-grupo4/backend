package ar.edu.unq.backend.user;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@RequestBody UserRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<UserResponseDTO> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDTO get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(@PathVariable Integer id,
                                  @RequestBody UserRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

}
