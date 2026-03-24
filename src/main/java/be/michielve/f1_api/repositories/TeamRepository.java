package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    @Query("""
            SELECT t FROM Team t
            LEFT JOIN t.driverTeamSeasons dts
            LEFT JOIN dts.season s
            WHERE s.seasonName = :seasonName OR s.seasonName IS NULL
            GROUP BY t.id, t.name, t.shortName, t.base, t.country
            ORDER BY SUM(CASE WHEN s.seasonName = :seasonName THEN dts.points ELSE 0 END) DESC
        """)
    List<Team> findAllTeamsBySeasonName(@Param("seasonName") String seasonName);

    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Team> findAllByNameContainingIgnoreCase(@Param("name") String name);

    Optional<Team> findByName(String name);

    Optional<Team> findByShortName(String shortName);

    List<Team> findAllByShortName(String shortName);
}
