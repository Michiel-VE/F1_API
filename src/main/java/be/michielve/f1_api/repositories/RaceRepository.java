package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RaceRepository extends JpaRepository<Race, UUID> {

    @Query("""
            SELECT DISTINCT r FROM Race r
            LEFT JOIN FETCH r.raceSeasons rs
            LEFT JOIN FETCH rs.season s
            WHERE s.seasonName = :seasonName
            ORDER BY r.raceStartDate ASC NULLS LAST
            """)
    List<Race> findAllRacesBySeasonName(@Param("seasonName") String seasonName);

    Optional<Race> findByNameAndRaceStartDateAndRaceEndDate(String name, LocalDate raceStartDate,
            LocalDate raceEndDate);

    Optional<Race> findByCountryIgnoreCase(String country);

    @Query(value = """
            SELECT r.* FROM f1_api.race r
            JOIN f1_api.race_status rs ON r.status_id = rs.id
            JOIN f1_api.race_season rsj ON r.id = rsj.race_id
            JOIN f1_api.season s ON rsj.season_id = s.id
            WHERE s.season_name = :year
            AND r.race_end_date < :now
            AND rs.name = 'COMPLETED'
            AND r.name NOT ILIKE '%TESTING%'
            AND r.name NOT ILIKE '%PRE-SEASON%'
            AND NOT EXISTS (
                SELECT 1 FROM f1_api.driver_race_result drr
                WHERE drr.race_id = r.id
            )
            ORDER BY r.race_end_date ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Race> findFirstFinishedRaceMissingResults(@Param("now") Timestamp now, @Param("year") String year);
}