package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.TeamPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamPredictionRepository extends JpaRepository<TeamPrediction, UUID> {

    @Query("SELECT t FROM TeamPrediction t LEFT JOIN FETCH t.predictedTeams WHERE t.user.id = :userId AND t.pool IS NULL")
    Optional<TeamPrediction> findPersonalByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM TeamPrediction t LEFT JOIN FETCH t.predictedTeams WHERE t.user.id = :userId AND t.pool.id = :poolId")
    Optional<TeamPrediction> findByUserIdAndPoolId(@Param("userId") UUID userId, @Param("poolId") UUID poolId);

    List<TeamPrediction> findByPoolId(UUID poolId);
    void deleteByPoolId(UUID poolId);
}