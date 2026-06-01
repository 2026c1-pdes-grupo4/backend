package ar.edu.unq.backend.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByAgencyProperty_Agency_AgencyId(Integer agencyId);

    List<Purchase> findByUser_UserId(Integer userId);

    @Query("""
            select p.user.userId as userId,
                   p.user.username as username,
                   count(p) as purchases
            from Purchase p
            group by p.user.userId, p.user.username
            order by count(p) desc, p.user.username asc
            """)
    List<TopBuyerProjection> findTopBuyers(Pageable pageable);

    @Query("""
            select p.agencyProperty.agency.agencyId as agencyId,
                   p.agencyProperty.agency.username as username,
                   count(p) as sales
            from Purchase p
            group by p.agencyProperty.agency.agencyId, p.agencyProperty.agency.username
            order by count(p) desc, p.agencyProperty.agency.username asc
            """)
    List<TopAgencySalesProjection> findTopAgenciesBySales(Pageable pageable);

    interface TopBuyerProjection {
        Integer getUserId();
        String getUsername();
        Long getPurchases();
    }

    interface TopAgencySalesProjection {
        Integer getAgencyId();
        String getUsername();
        Long getSales();
    }
}

