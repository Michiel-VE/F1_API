package be.michielve.f1_api.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.michielve.f1_api.models.SeasonPrediction;

public interface SeasonPredictionRepository extends JpaRepository<SeasonPrediction, UUID> {
    boolean existsByUserId(UUID userId);
}
