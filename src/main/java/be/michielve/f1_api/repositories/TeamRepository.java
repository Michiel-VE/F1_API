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
            SELECT DISTINCT t FROM Team t
            JOIN t.driverTeamSeasons dts
            JOIN dts.season s
            WHERE s.seasonName = :seasonName
            """)
    List<Team> findAllTeamsBySeasonName(@Param("seasonName") String seasonName);

    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Team> findAllByNameContainingIgnoreCase(@Param("name") String name);

    Optional<Team> findByName(String name);
    Optional<Team> findByShortName(String shortName);
}
