package be.kwallie.F1.repository;

import be.kwallie.F1.models.Team;
import be.kwallie.F1.models.response.TeamWithDriverResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<TeamWithDriverResponse> getTeamsByDrivers();
}
