package be.michielve.f1_api.services;

import be.michielve.f1_api.convertors.RaceConverter;
import be.michielve.f1_api.models.response.RaceResponse;
import be.michielve.f1_api.repositories.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RaceService {
    private static final Logger logger = LoggerFactory.getLogger(RaceService.class);

    private final RaceRepository raceRepository;
    private final RaceConverter raceConverter;

    public List<RaceResponse> getAllRacesForSeason(String season) {
        logger.info("Attempting to retrieve all races for season: {}", season);
        List<RaceResponse> races = raceRepository.findAllRacesBySeasonName(season).stream()
                .map(raceConverter::raceWithSeasonResponse)
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} races for season: {}", races.size(), season);
        return races;
    }
}