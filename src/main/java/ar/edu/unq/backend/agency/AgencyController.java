package ar.edu.unq.backend.agency;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agencies")
@PreAuthorize("hasRole('ADMIN')")
public class AgencyController {

    private final AgencyService service;

    public AgencyController(AgencyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyResponseDTO create(@RequestBody AgencyRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<AgencyResponseDTO> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AgencyResponseDTO get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public AgencyResponseDTO update(@PathVariable Integer id, @RequestBody AgencyRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
