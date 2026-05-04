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
        // TODO: Consultar usuarios registrados
        return List.of();
    }


    @GetMapping("/agencies")
    public List<AgencyResponseDTO> agencies() {
        // TODO: Consultar agencias registradas
        return List.of();
    }

    /*
    TODO: Obtener reportes de utilización del sistema:
     - Top 5 usuarios con más compras
     - Top 5 propiedades mejor rankeadas
     - Top 5 inmobiliarias con más ventas
     Ver los puntajes y observaciones de los usuarios
     */

}
