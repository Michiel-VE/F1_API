package be.kwallie.F1.repository;

import be.kwallie.F1.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findAllByOrderByPointsDescLastNameAsc();
    @Query("SELECT d, t.picture, t.name FROM Driver d JOIN Team t on d.team.id = t.id where d.id = :driver")
    Optional<Driver> findDriverAndTeam(Long driver);
}
