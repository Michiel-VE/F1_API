package be.michielve.f1_api.repositories;


import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RaceRepository extends JpaRepository<Race, UUID> {

    @Query("""
            SELECT DISTINCT r FROM Race r
            JOIN r.raceSeasons rs
            JOIN rs.season s
            WHERE s.seasonName = :seasonName
            """)
    List<Race> findAllRacesBySeasonName(@Param("seasonName") String seasonName);

    Optional<Race> findByNameAndRaceStartDateAndRaceEndDate(String name, LocalDate raceStartDate, LocalDate raceEndDate);

}
