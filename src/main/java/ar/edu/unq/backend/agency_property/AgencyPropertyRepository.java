package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.property.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgencyPropertyRepository extends JpaRepository<AgencyProperty, Integer> {

    List<AgencyProperty> findByAgency_AgencyId(Integer agencyId);

    boolean existsByAgency_AgencyIdAndProperty_PropertyId(Integer agencyId, Integer propertyId);

    @Query("""
            select ap from AgencyProperty ap
            where ap.property.available = true
              and (:city is null or lower(ap.property.city) = lower(:city))
              and (:province is null or lower(ap.property.province) = lower(:province))
              and (:propertyType is null or ap.property.propertyType = :propertyType)
              and (:rooms is null or ap.property.rooms = :rooms)
              and (:priceMin is null or ap.listedPrice >= :priceMin)
              and (:priceMax is null or ap.listedPrice <= :priceMax)
              and (:keyword is null
                   or lower(ap.property.address) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.description) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.city) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.province) like lower(concat('%', :keyword, '%')))
            order by ap.property.address asc, ap.agency.username asc
            """)
    List<AgencyProperty> searchActiveListings(
            @Param("city") String city,
            @Param("province") String province,
            @Param("propertyType") PropertyType propertyType,
            @Param("rooms") Integer rooms,
            @Param("priceMin") Double priceMin,
            @Param("priceMax") Double priceMax,
            @Param("keyword") String keyword
    );
}
