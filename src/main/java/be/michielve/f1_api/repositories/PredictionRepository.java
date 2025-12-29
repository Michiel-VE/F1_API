package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    Optional<Prediction> findByUserIdAndRaceId(UUID userId, UUID raceId);

    boolean existsByUserIdAndRaceId(UUID userId, UUID raceId);
}
