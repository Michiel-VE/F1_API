package be.kwallie.F1.convertors;

import be.kwallie.F1.models.Race;
import be.kwallie.F1.models.response.RaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RaceJsonConverter {
    public RaceResponse raceResponseConvert(Race race){
        return RaceResponse.builder()
                .id(race.getId())
                .name(race.getName())
                .lat(race.getLat())
                .lon(race.getLon())
                .city(race.getCity())
                .country(race.getCountry())
                .date(race.getDate())
                .timezone(race.getTimezone())
                .active(race.isActive())
                .build();
    }
}
