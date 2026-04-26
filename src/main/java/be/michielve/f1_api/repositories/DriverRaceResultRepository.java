package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverRaceResult;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverRaceResultRepository extends JpaRepository<DriverRaceResult, UUID> {

    boolean existsByDriverAndRaceAndSeason(Driver driver, Race race, Season season);

    @Query("SELECT drr FROM DriverRaceResult drr " +
            "JOIN drr.driver d " +
            "JOIN drr.season s " +
            "JOIN drr.race r " +
            "WHERE d.id = :id " +
            "AND s.seasonName = :seasonName " +
            "ORDER BY r.raceStartDate ASC")
    List<DriverRaceResult> findByDriverIdAndSeason(@Param("id") UUID id,
            @Param("seasonName") String seasonName);
}