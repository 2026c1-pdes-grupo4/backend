package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.agency.AgencyMapper;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.agency.AgencyResponseDTO;
import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
import ar.edu.unq.backend.user.UserMapper;
import ar.edu.unq.backend.user.UserRepository;
import ar.edu.unq.backend.user.UserResponseDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que expone operaciones administrativas de solo lectura sobre favoritos y compras.
 */
@Service
public class AdminService {

    private final FavoriteRepository favoriteRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final UserMapper userMapper;
    private final AgencyMapper agencyMapper;

    public AdminService(FavoriteRepository favoriteRepository,
                        PurchaseRepository purchaseRepository,
                        UserRepository userRepository,
                        AgencyRepository agencyRepository,
                        UserMapper userMapper,
                        AgencyMapper agencyMapper) {
        this.favoriteRepository = favoriteRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
        this.userMapper = userMapper;
        this.agencyMapper = agencyMapper;
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

    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToResponse)
                .toList();
    }

    public List<AgencyResponseDTO> findAllAgencies() {
        return agencyRepository.findAll()
                .stream()
                .map(agencyMapper::mapToResponse)
                .toList();
    }

    public List<TopBuyerDTO> topBuyers() {
        return purchaseRepository.findTopBuyers(PageRequest.of(0, 5))
                .stream()
                .map(row -> new TopBuyerDTO(row.getUserId(), row.getUsername(), row.getPurchases()))
                .toList();
    }

    public List<TopRankedPropertyDTO> topRankedProperties() {
        return favoriteRepository.findTopRankedProperties(PageRequest.of(0, 5))
                .stream()
                .map(row -> new TopRankedPropertyDTO(row.getPropertyId(), row.getAddress(), row.getAverageScore(), row.getRatings()))
                .toList();
    }

    public List<TopAgencySalesDTO> topAgenciesBySales() {
        return purchaseRepository.findTopAgenciesBySales(PageRequest.of(0, 5))
                .stream()
                .map(row -> new TopAgencySalesDTO(row.getAgencyId(), row.getUsername(), row.getSales()))
                .toList();
    }
}
