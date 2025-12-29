package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
    Optional<Season> findBySeasonName(String year);

}
