package be.michielve.f1_api.repositories;

import be.michielve.f1_api.models.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PoolRepository extends JpaRepository<Pool, UUID> {
    Optional<Pool> findByInviteCode(String inviteCode);

    @Query("SELECT p FROM Pool p JOIN p.members m WHERE m.id = :userId")
    List<Pool> findPoolsByUserId(@Param("userId") UUID userId);
}