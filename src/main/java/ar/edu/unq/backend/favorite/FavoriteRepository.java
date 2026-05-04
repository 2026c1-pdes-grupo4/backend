package ar.edu.unq.backend.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findByUser_UserId(Integer userId);

    boolean existsByUser_UserIdAndAgencyProperty_AgencyPropertyId(Integer userId, Integer agencyPropertyId);
}

