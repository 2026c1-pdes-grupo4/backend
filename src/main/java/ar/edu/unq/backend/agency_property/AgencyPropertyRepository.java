package ar.edu.unq.backend.agency_property;

import ar.edu.unq.backend.property.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgencyPropertyRepository extends JpaRepository<AgencyProperty, Integer> {

    List<AgencyProperty> findByAgency_AgencyIdAndDeletedFalse(Integer agencyId);

    boolean existsByAgency_AgencyIdAndProperty_PropertyIdAndDeletedFalse(Integer agencyId, Integer propertyId);

    boolean existsByProperty_PropertyIdAndDeletedFalse(Integer propertyId);

    @Query(value = """
            select ap from AgencyProperty ap
            where ap.deleted = false
              and ap.property.available = true
              and (:city is null or lower(ap.property.city) like lower(concat('%', :city, '%')))
              and (:province is null or lower(ap.property.province) like lower(concat('%', :province, '%')))
              and (:propertyType is null or ap.property.propertyType = :propertyType)
              and (:roomsMin is null or ap.property.rooms >= :roomsMin)
              and (:roomsMax is null or ap.property.rooms <= :roomsMax)
              and (:priceMin is null or ap.listedPrice >= :priceMin)
              and (:priceMax is null or ap.listedPrice <= :priceMax)
              and (:keyword is null
                   or lower(ap.property.address) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.description) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.city) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.province) like lower(concat('%', :keyword, '%')))
            order by ap.property.address asc, ap.agency.username asc
            """,
            countQuery = """
            select count(ap) from AgencyProperty ap
            where ap.deleted = false
              and ap.property.available = true
              and (:city is null or lower(ap.property.city) like lower(concat('%', :city, '%')))
              and (:province is null or lower(ap.property.province) like lower(concat('%', :province, '%')))
              and (:propertyType is null or ap.property.propertyType = :propertyType)
              and (:roomsMin is null or ap.property.rooms >= :roomsMin)
              and (:roomsMax is null or ap.property.rooms <= :roomsMax)
              and (:priceMin is null or ap.listedPrice >= :priceMin)
              and (:priceMax is null or ap.listedPrice <= :priceMax)
              and (:keyword is null
                   or lower(ap.property.address) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.description) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.city) like lower(concat('%', :keyword, '%'))
                   or lower(ap.property.province) like lower(concat('%', :keyword, '%')))
            """)
    Page<AgencyProperty> searchActiveListings(
            @Param("city") String city,
            @Param("province") String province,
            @Param("propertyType") PropertyType propertyType,
            @Param("roomsMin") Integer roomsMin,
            @Param("roomsMax") Integer roomsMax,
            @Param("priceMin") Double priceMin,
            @Param("priceMax") Double priceMax,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
