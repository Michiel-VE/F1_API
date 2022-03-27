package be.kwallie.F1.repository;

import be.kwallie.F1.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query(value = "select * from driver order by points desc limit 3", nativeQuery = true)
    List<Driver> getTopThree();
}
