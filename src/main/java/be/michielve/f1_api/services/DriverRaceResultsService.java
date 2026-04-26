package be.michielve.f1_api.services;

import be.michielve.f1_api.convertors.DriverConverter;
import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.DriverRaceResult;
import be.michielve.f1_api.models.response.DriverRaceResultsResponse;
import be.michielve.f1_api.repositories.DriverRaceResultRepository;
import be.michielve.f1_api.repositories.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverRaceResultsService {
    private static final Logger logger = LoggerFactory.getLogger(DriverRaceResultsService.class);

    private final DriverRaceResultRepository driverRaceResultRepository;
    private final DriverRepository driverRepository;
    private final DriverConverter driverConverter;

    @Transactional(readOnly = true)
    public DriverRaceResultsResponse getDriverRaceResults(UUID id, String season) {
        logger.info("Retrieving race results for driver: {} in season: {}", id, season);

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));

        List<DriverRaceResult> results = driverRaceResultRepository.findByDriverIdAndSeason(id, season);

        return driverConverter.toDriverRaceResultsResponse(driver, results, season);
    }
}