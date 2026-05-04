package ar.edu.unq.backend.purchase;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponseDTO buy(@RequestBody PurchaseRequestDTO dto) {
        return service.buy(dto);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('BUYER')")
    public List<PurchaseResponseDTO> myPurchases() {
        return service.findForCurrentUser();
    }

    @GetMapping("/agency/me")
    @PreAuthorize("hasRole('AGENCY')")
    public List<PurchaseResponseDTO> agencySales() {
        return service.findForCurrentAgency();
    }

}
