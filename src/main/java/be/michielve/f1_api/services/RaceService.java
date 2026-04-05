package be.michielve.f1_api.services;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import be.michielve.f1_api.convertors.RaceConverter;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.response.RaceResponse;
import be.michielve.f1_api.repositories.RaceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceService {
    private static final Logger logger = LoggerFactory.getLogger(RaceService.class);

    private final RaceRepository raceRepository;
    private final RaceConverter raceConverter;

    public List<RaceResponse> getAllRacesForSeason(String season) {
        logger.info("Attempting to retrieve all races for season: {}", season);

        return raceRepository.findAllRacesBySeasonName(season).stream()
                .sorted(Comparator.comparing(
                        Race::getRaceStartDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(raceConverter::raceWithSeasonResponse)
                .collect(Collectors.toList());
    }
}