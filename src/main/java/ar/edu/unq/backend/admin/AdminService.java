package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final FavoriteRepository favoriteRepository;
    private final PurchaseRepository purchaseRepository;

    public AdminService(FavoriteRepository favoriteRepository,
                           PurchaseRepository purchaseRepository) {
        this.favoriteRepository = favoriteRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public List<Favorite> findAllFavorites() {
        return favoriteRepository.findAll();
    }

    public List<Purchase> findAllPurchases() {
        return purchaseRepository.findAll();
    }
}
