package ar.edu.unq.backend.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, Integer> {
    Optional<Agency> findByUsername(String username);

    Optional<Agency> findByUsernameAndDeletedFalse(String username);

    Optional<Agency> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Agency> findAllByDeletedFalse();
}

