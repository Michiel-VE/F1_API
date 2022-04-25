package be.kwallie.F1.services;

import be.kwallie.F1.models.response.RaceResponse;

import java.util.List;

public interface RaceService {
    List<RaceResponse> getAllRaces();
    int getPassedRaces();
}
