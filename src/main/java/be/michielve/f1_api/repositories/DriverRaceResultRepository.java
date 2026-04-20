package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverRaceResult;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRaceResultRepository extends JpaRepository<DriverRaceResult, UUID> {

    boolean existsByDriverAndRaceAndSeason(Driver driver, Race race, Season season);
}