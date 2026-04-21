package ar.edu.unq.backend.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgencyPropertyRepository extends JpaRepository<AgencyProperty, Integer> {

    List<AgencyProperty> findByAgency_AgencyId(Integer agencyId);

    List<AgencyProperty> findByActiveTrue();
}
