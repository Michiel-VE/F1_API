package be.michielve.f1_api.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import be.michielve.f1_api.convertors.RaceConverter;
import be.michielve.f1_api.models.response.RaceResponse;
import be.michielve.f1_api.repositories.RaceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RaceService {
    private final RaceRepository raceRepository;
    private final RaceConverter raceConverter;

    @Cacheable(cacheResolver = "raceCacheResolver", key = "'season:' + #season")
    public List<RaceResponse> getAllRacesForSeason(String season) {
        return raceRepository.findAllRacesBySeasonName(season).stream()
                .map(raceConverter::raceWithSeasonResponse)
                .collect(Collectors.toList());
    }
}