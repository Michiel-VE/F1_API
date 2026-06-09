package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    // Using a native query keeps sorting clean, efficient, and prevents JPQL fetch collection syntax exceptions
    @Query(value = """
            SELECT DISTINCT t.* FROM f1_api.team t
            LEFT JOIN f1_api.driver_team_season dts ON t.id = dts.team_id
            LEFT JOIN f1_api.season s ON dts.season_id = s.id
            WHERE s.season_name = :seasonName OR s.season_name IS NULL
            ORDER BY dts.points DESC NULLS LAST
            """, nativeQuery = true)
    List<Team> findAllTeamsBySeasonName(@Param("seasonName") String seasonName);

    @Query(value = """
            SELECT DISTINCT t.* FROM f1_api.team t 
            WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """, nativeQuery = true)
    List<Team> findAllByNameContainingIgnoreCase(@Param("name") String name);

    Optional<Team> findByName(String name);

    Optional<Team> findByShortName(String shortName);

    List<Team> findAllByShortName(String shortName);
}