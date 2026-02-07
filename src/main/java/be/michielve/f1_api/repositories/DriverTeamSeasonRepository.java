package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverTeamSeason;
import be.michielve.f1_api.models.Season;
import be.michielve.f1_api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}