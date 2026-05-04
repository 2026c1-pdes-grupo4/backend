package ar.edu.unq.backend.favorite;


import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@PreAuthorize("hasRole('BUYER')")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteResponseDTO save(@RequestBody FavoriteRequestDTO dto) {
        return service.save(dto);
    }

    @GetMapping("/me")
    public List<FavoriteResponseDTO> myFavorites() {
        return service.findForCurrentUser();
    }

    @PutMapping("/{id}")
    public FavoriteResponseDTO update(@PathVariable Integer id, @RequestBody FavoriteRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

}
