package be.michielve.f1_api.services;

import be.michielve.f1_api.models.*;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.models.request.CreateSeasonPredictionRequest;
import be.michielve.f1_api.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final SeasonPredictionRepository seasonPredictionRepository;
    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final SeasonRepository seasonRepository;
    private final DriverTeamSeasonRepository driverTeamSeasonRepository;

    @Transactional
    public Prediction createPrediction(UUID userId, CreatePredictionRequest request) {
        UUID nonNullUserId = Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");
        UUID raceId = Objects.requireNonNull(request.getRaceId(), "Race ID cannot be null");

        List<UUID> predictedDriverIds = request.getPredictedDrivers();
        validateUniqueIds(predictedDriverIds, 10, "driver");

        List<UUID> existingDriverIds = driverRepository.findAllById(predictedDriverIds)
                .stream().map(Driver::getId).toList();
        validateExistence(predictedDriverIds, existingDriverIds, "Driver(s)");

        if (predictionRepository.existsByUserIdAndRaceId(nonNullUserId, raceId)) {
            throw new IllegalArgumentException("You already submitted a prediction for this race.");
        }

        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new IllegalArgumentException("Race not found"));
        User user = userRepository.findById(nonNullUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Prediction prediction = Prediction.builder()
                .user(user)
                .race(race)
                .predictedDrivers(convertListToJsonString(predictedDriverIds))
                .build();

        return predictionRepository.save(prediction);
    }

    @Transactional
    public SeasonPrediction createTeamPrediction(UUID userId, CreateSeasonPredictionRequest request) {
        UUID nonNullUserId = Objects.requireNonNull(userId, "You need to be logged in to submit a season prediction.");
        Objects.requireNonNull(request, "Request cannot be null");

        // 1. Get the Season ID for the current year (e.g., "2026")
        String currentYearName = String.valueOf(LocalDate.now().getYear());
        Season currentSeason = seasonRepository.findBySeasonName(currentYearName)
                .orElseThrow(
                        () -> new IllegalArgumentException("Season " + currentYearName + " not found in database"));

        // 1. Get all valid teams for this specific season
        List<UUID> validTeamIdsForSeason = driverTeamSeasonRepository.findAllBySeasonId(currentSeason.getId())
                .stream()
                .map(dst -> dst.getTeam().getId())
                .distinct()
                .toList();

        int actualTeamCount = validTeamIdsForSeason.size(); // This will be 10 or 11 depending on the DB
        List<UUID> predictedTeamIds = request.getPredictedTeams();

        validateUniqueIds(predictedTeamIds, actualTeamCount, "team");
        validateExistence(predictedTeamIds, validTeamIdsForSeason, "Team(s) for season " + currentYearName);

        // 3. Check for existing prediction
        if (seasonPredictionRepository.existsByUserId(nonNullUserId)) {
            throw new IllegalArgumentException("You already submitted your season team predictions.");
        }

        SeasonPrediction prediction = SeasonPrediction.builder()
                .userId(nonNullUserId)
                .predicted_teams(convertListToJsonString(predictedTeamIds))
                .build();

        return seasonPredictionRepository.save(prediction);
    }

    private void validateUniqueIds(List<UUID> ids, int expectedSize, String type) {
        if (ids == null || ids.size() != expectedSize) {
            throw new IllegalArgumentException(
                    "You must provide exactly " + expectedSize + " unique " + type + " IDs.");
        }
        Set<UUID> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() != expectedSize) {
            List<UUID> duplicates = findDuplicates(ids);
            throw new IllegalArgumentException("Duplicate " + type + " IDs found: " + duplicates);
        }
    }

    private void validateExistence(List<UUID> requested, List<UUID> existing, String label) {
        List<UUID> missing = requested.stream()
                .filter(id -> !existing.contains(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(label + " not found or invalid for current season: " + missing);
        }
    }

    private List<UUID> findDuplicates(List<UUID> list) {
        Set<UUID> seen = new HashSet<>();
        return list.stream()
                .filter(id -> !seen.add(id))
                .distinct()
                .toList();
    }

    private String convertListToJsonString(List<UUID> ids) {
        return ids.stream()
                .map(id -> "\"" + id.toString() + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}