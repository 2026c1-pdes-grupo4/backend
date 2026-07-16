package ar.edu.unq.backend.property;

import ar.edu.unq.backend.agency_property.AgencyPropertyResponseDTO;
import ar.edu.unq.backend.common.dto.PagedResultDTO;
import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PropertyResponseDTO> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PropertyResponseDTO get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @GetMapping("/find-by-cadastral")
    @PreAuthorize("hasRole('AGENCY')")
    public PropertyResponseDTO findByCadastral(@RequestParam String circumscription, @RequestParam String section,
                                                @RequestParam String block, @RequestParam String parcel) {
        return service.findByCadastral(circumscription, section, block, parcel);
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENCY')")
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponseDTO create(@RequestBody PropertyRequestDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENCY')")
    public PropertyResponseDTO update(@PathVariable Integer id, @RequestBody PropertyRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENCY')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public PagedResultDTO<AgencyPropertyResponseDTO> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) Integer roomsMin,
            @RequestParam(required = false) Integer roomsMax,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(city, province, propertyType, roomsMin, roomsMax, priceMin, priceMax, keyword, page, size);
    }
}