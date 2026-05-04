package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que expone operaciones administrativas de solo lectura sobre favoritos y compras.
 */
@Service
public class AdminService {

    private final FavoriteRepository favoriteRepository;
    private final PurchaseRepository purchaseRepository;

    public AdminService(FavoriteRepository favoriteRepository,
                           PurchaseRepository purchaseRepository) {
        this.favoriteRepository = favoriteRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Devuelve la lista de todos los favoritos registrados.
     *
     * @return lista completa de favoritos
     */
    public List<Favorite> findAllFavorites() {
        return favoriteRepository.findAll();
    }

    /**
     * Devuelve la lista de todas las compras registradas.
     *
     * @return lista completa de compras
     */
    public List<Purchase> findAllPurchases() {
        return purchaseRepository.findAll();
    }
}
