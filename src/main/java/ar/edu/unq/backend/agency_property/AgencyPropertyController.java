package ar.edu.unq.backend.agency_property;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agency-properties")
public class AgencyPropertyController {

    private final AgencyPropertyService service;

    public AgencyPropertyController(AgencyPropertyService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENCY')")
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyPropertyResponseDTO publish(@RequestBody AgencyPropertyRequestDTO dto) {
        return service.publish(dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AgencyPropertyResponseDTO get(@PathVariable Integer id) {
        return service.findById(id);
    }


    @GetMapping("/agency/me")
    @PreAuthorize("hasRole('AGENCY')")
    public List<AgencyPropertyResponseDTO> myPublications() {
        return service.findByCurrentAgency();
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENCY')")
    public AgencyPropertyResponseDTO update(@PathVariable Integer id, @RequestBody AgencyPropertyRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENCY')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

}