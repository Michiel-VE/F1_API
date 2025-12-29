package be.michielve.f1_api.convertors;

import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.response.RaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RaceConverter {
    public RaceResponse raceWithSeasonResponse(Race race){
        return RaceResponse.builder()
                .id(race.getId())
                .name(race.getName())
                .country(race.getCountry())
                .startDay(race.getRaceStartDate())
                .endDay(race.getRaceEndDate())
                .created_at(race.getCreated_at())
                .build();
    }
}
