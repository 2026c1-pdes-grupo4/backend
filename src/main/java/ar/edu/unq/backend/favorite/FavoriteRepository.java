package ar.edu.unq.backend.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findByUser_UserId(Integer userId);

    boolean existsByUser_UserIdAndAgencyProperty_AgencyPropertyId(Integer userId, Integer agencyPropertyId);

    @Query("""
            select f.agencyProperty.property.propertyId as propertyId,
                   f.agencyProperty.property.address as address,
                   avg(f.score) as averageScore,
                   count(f) as ratings
            from Favorite f
            where f.score is not null
            group by f.agencyProperty.property.propertyId, f.agencyProperty.property.address
            order by avg(f.score) desc, f.agencyProperty.property.address asc
            """)
    List<TopRankedPropertyProjection> findTopRankedProperties(Pageable pageable);

    interface TopRankedPropertyProjection {
        Integer getPropertyId();
        String getAddress();
        Double getAverageScore();
        Long getRatings();
    }
}

