package ar.edu.unq.backend.property;

import ar.edu.unq.backend.property.dto.PropertyRequestDTO;
import ar.edu.unq.backend.property.dto.PropertyResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @GetMapping
    public List<PropertyResponseDTO> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PropertyResponseDTO get(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponseDTO create(@RequestBody PropertyRequestDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public PropertyResponseDTO update(@PathVariable Integer id, @RequestBody PropertyRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}