package be.kwallie.F1.repository;

import be.kwallie.F1.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT t, sum(d.points) FROM Team t join t.drivers d group by t.id order by sum(d.points) desc")
    List<Team> getTeamsByDrivers();
}
