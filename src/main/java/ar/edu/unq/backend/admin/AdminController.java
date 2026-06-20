package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.agency.AgencyResponseDTO;
import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.user.UserResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/favorites")
    public List<Favorite> listFavorites() {
        return adminService.findAllFavorites();
    }

    @GetMapping("/purchases")
    public List<Purchase> listPurchases() {
        return adminService.findAllPurchases();
    }


    @GetMapping("/users")
    public List<UserResponseDTO> users() {
        return adminService.findAllUsers();
    }

    @GetMapping("/agencies")
    public List<AgencyResponseDTO> agencies() {
        return adminService.findAllAgencies();
    }

    @GetMapping("/reports/top-buyers")
    public List<TopBuyerDTO> topBuyers() {
        return adminService.topBuyers();
    }

    @GetMapping("/reports/top-ranked-properties")
    public List<TopRankedPropertyDTO> topRankedProperties() {
        return adminService.topRankedProperties();
    }

    @GetMapping("/reports/top-agencies-sales")
    public List<TopAgencySalesDTO> topAgenciesBySales() {
        return adminService.topAgenciesBySales();
    }

}
