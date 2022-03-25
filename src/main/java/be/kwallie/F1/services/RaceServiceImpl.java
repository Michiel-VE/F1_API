package be.kwallie.F1.services;

import be.kwallie.F1.convertors.RaceJsonConverter;
import be.kwallie.F1.models.response.RaceResponse;
import be.kwallie.F1.repository.RaceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class RaceServiceImpl implements RaceService {
    private final RaceRepository raceRepository;
    private final RaceJsonConverter raceJsonConverter;

    @Override
    public List<RaceResponse> getAllRaces() {
        return raceRepository.findAll().stream()
                .map(raceJsonConverter::raceResponseConvert)
                .collect(Collectors.toList());
    }
}
