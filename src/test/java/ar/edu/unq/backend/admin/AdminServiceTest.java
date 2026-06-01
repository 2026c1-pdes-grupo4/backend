package ar.edu.unq.backend.admin;

import ar.edu.unq.backend.agency.AgencyMapper;
import ar.edu.unq.backend.agency.AgencyRepository;
import ar.edu.unq.backend.agency.AgencyResponseDTO;
import ar.edu.unq.backend.favorite.Favorite;
import ar.edu.unq.backend.favorite.FavoriteRepository;
import ar.edu.unq.backend.purchase.Purchase;
import ar.edu.unq.backend.purchase.PurchaseRepository;
import ar.edu.unq.backend.user.User;
import ar.edu.unq.backend.user.UserMapper;
import ar.edu.unq.backend.user.UserRepository;
import ar.edu.unq.backend.user.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AgencyRepository agencyRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AgencyMapper agencyMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void topBuyersMapsProjection() {
        PurchaseRepository.TopBuyerProjection row = new PurchaseRepository.TopBuyerProjection() {
            @Override
            public Integer getUserId() { return 1; }

            @Override
            public String getUsername() { return "buyer"; }

            @Override
            public Long getPurchases() { return 3L; }
        };

        when(purchaseRepository.findTopBuyers(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(List.of(row));

        List<TopBuyerDTO> result = adminService.topBuyers();

        assertEquals(1, result.size());
        assertEquals("buyer", result.get(0).username());
        assertEquals(3L, result.get(0).purchases());
    }

    @Test
    void topRankedPropertiesMapsProjection() {
        FavoriteRepository.TopRankedPropertyProjection row = new FavoriteRepository.TopRankedPropertyProjection() {
            @Override
            public Integer getPropertyId() { return 7; }

            @Override
            public String getAddress() { return "A"; }

            @Override
            public Double getAverageScore() { return 9.5; }

            @Override
            public Long getRatings() { return 2L; }
        };

        when(favoriteRepository.findTopRankedProperties(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(List.of(row));

        List<TopRankedPropertyDTO> result = adminService.topRankedProperties();

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).propertyId());
        assertEquals(9.5, result.get(0).averageScore());
    }

    @Test
    void topAgenciesBySalesMapsProjection() {
        PurchaseRepository.TopAgencySalesProjection row = new PurchaseRepository.TopAgencySalesProjection() {
            @Override
            public Integer getAgencyId() { return 4; }

            @Override
            public String getUsername() { return "inmo"; }

            @Override
            public Long getSales() { return 6L; }
        };

        when(purchaseRepository.findTopAgenciesBySales(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(List.of(row));

        List<TopAgencySalesDTO> result = adminService.topAgenciesBySales();

        assertEquals(1, result.size());
        assertEquals("inmo", result.get(0).username());
        assertEquals(6L, result.get(0).sales());
    }

    @Test
    void findAllUsersMapsEntities() {
        User entity = new User();
        UserResponseDTO dto = new UserResponseDTO();
        when(userRepository.findAll()).thenReturn(List.of(entity));
        when(userMapper.mapToResponse(entity)).thenReturn(dto);

        assertEquals(1, adminService.findAllUsers().size());
    }

    @Test
    void findAllAgenciesMapsEntities() {
        ar.edu.unq.backend.agency.Agency entity = new ar.edu.unq.backend.agency.Agency();
        AgencyResponseDTO dto = new AgencyResponseDTO();
        when(agencyRepository.findAll()).thenReturn(List.of(entity));
        when(agencyMapper.mapToResponse(entity)).thenReturn(dto);

        assertEquals(1, adminService.findAllAgencies().size());
    }

    @Test
    void favoritesAndPurchasesListAll() {
        when(favoriteRepository.findAll()).thenReturn(List.of(new Favorite()));
        when(purchaseRepository.findAll()).thenReturn(List.of(new Purchase()));

        assertEquals(1, adminService.findAllFavorites().size());
        assertEquals(1, adminService.findAllPurchases().size());
    }
}

