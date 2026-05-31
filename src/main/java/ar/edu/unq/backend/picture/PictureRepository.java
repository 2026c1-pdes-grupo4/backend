package ar.edu.unq.backend.picture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Integer> {
    List<Picture> findByAgencyProperty_AgencyPropertyId(Integer agencyPropertyId);
}

