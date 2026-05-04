package ar.edu.unq.backend.purchase;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByAgencyProperty_Agency_AgencyId(Integer agencyId);

    List<Purchase> findByUser_UserId(Integer userId);
}

