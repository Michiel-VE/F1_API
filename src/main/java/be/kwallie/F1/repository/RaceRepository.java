package be.kwallie.F1.repository;

import be.kwallie.F1.models.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    @Query(value = "select count(r) from Race r where r.date < current_date")
    int countOfPassedRaces();

    @Query(value = "select r from Race r where r.date > current_date order by  r.date")
    List<Race> getCalender();

    List<Race> findAllByOrderByDateAsc();

}
