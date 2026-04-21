package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
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
}
