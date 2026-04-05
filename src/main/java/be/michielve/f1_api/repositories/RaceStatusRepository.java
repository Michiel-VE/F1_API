package be.michielve.f1_api.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.michielve.f1_api.models.RaceStatus;

@Repository
public interface RaceStatusRepository extends JpaRepository<RaceStatus, UUID> {
    Optional<RaceStatus> findByName(String name);

}