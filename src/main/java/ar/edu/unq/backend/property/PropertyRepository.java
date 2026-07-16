package ar.edu.unq.backend.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Integer>, JpaSpecificationExecutor<Property> {
    Optional<Property> findByCircumscriptionAndSectionAndBlockAndParcel(String circumscription, String section, String block, String parcel);
}