package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    @Query("""
            SELECT DISTINCT d FROM Driver d
            LEFT JOIN FETCH d.driverTeamSeasons dts
            LEFT JOIN FETCH dts.team t
            LEFT JOIN FETCH dts.season s
            WHERE s.seasonName = :season
            ORDER BY dts.points desc
            """)
    List<Driver> findAllWithTeamsAndSeasons(@Param("season") String season);

    @Query("""
            SELECT DISTINCT d FROM Driver d
            LEFT JOIN FETCH d.driverTeamSeasons dts
            LEFT JOIN FETCH dts.season s
            WHERE s.seasonName = :season
            ORDER BY dts.points desc
             """)
    List<Driver> findAllBySeasonName(@Param("season") String season);

    Optional<Driver> findByLastnameIgnoreCase(String lastName);

    Optional<Driver> findByFirstnameIgnoreCase(String firstName);

    Optional<Driver> findByFirstnameAndLastnameIgnoreCase(String firstName, String lastName);

    Optional<Driver> findByPermanentNumber(Integer permanentNumber);
}