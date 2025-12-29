package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.RaceSeason;
import be.michielve.f1_api.models.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceSeasonRepository extends JpaRepository<RaceSeason, Long> {

    boolean existsByRaceAndSeason(Race race, Season season);

}
