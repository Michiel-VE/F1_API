package be.michielve.f1_api.repositories;

import be.michielve.f1_api.interfaces.TeamWithPoints;
import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverTeamSeason;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverTeamSeasonRepository extends JpaRepository<DriverTeamSeason, UUID> {
    Optional<DriverTeamSeason> findByDriverAndSeasonAndTeam(Driver driver, Season season, Team team);

    @Query(value = """
            WITH RankedResults AS (
                SELECT
                    s.season_name,
                    d.lastname AS driver_surname,
                    dts.points,
                    ROW_NUMBER() OVER (PARTITION BY s.id ORDER BY dts.points DESC) AS position
                FROM
                    f1_api.driver d
                JOIN
                    f1_api.driver_team_season dts ON dts.driver_id = d.id
                JOIN
                    f1_api.season s ON dts.season_id = s.id
            )
            SELECT
                position
            FROM
                RankedResults
            WHERE
                LOWER(driver_surname) = LOWER(:driverLastName)
                AND season_name = :seasonName
            """, nativeQuery = true)
    Integer findDriverPositionByLastNameAndSeason(
            @Param("driverLastName") String driverLastName,
            @Param("seasonName") String seasonName);

    @Query(value = """
            SELECT
                t.id,
                t.name,
                t.short_name,
                t.country,
                t.base,
                t.created_at,
                SUM(dts.points) AS total_points
            FROM f1_api.team t
            JOIN f1_api.driver_team_season dts ON t.id = dts.team_id
            JOIN f1_api.season s ON s.id = dts.season_id
            WHERE s.season_name = :seasonName
            GROUP BY
                t.id,
                t.name,
                t.short_name,
                t.country,
                t.base,
                t.created_at
            ORDER BY total_points DESC;
                        """, nativeQuery = true)
    List<TeamWithPoints> findAllTeamsBySeasonName(@Param("seasonName") String seasonName);

    List<DriverTeamSeason> findAllBySeasonId(UUID seasonId);
}