package ar.edu.unq.backend.agency_property;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgencyPropertyRepository extends JpaRepository<AgencyProperty, Integer> {

    List<AgencyProperty> findByAgency_AgencyId(Integer agencyId);

    boolean existsByAgency_AgencyIdAndProperty_PropertyId(Integer agencyId, Integer propertyId);
}
