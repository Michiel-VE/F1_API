package be.michielve.f1_api.services;

import be.michielve.f1_api.models.Driver;
import be.michielve.f1_api.models.Prediction;
import be.michielve.f1_api.models.Race;
import be.michielve.f1_api.models.User;
import be.michielve.f1_api.models.request.CreatePredictionRequest;
import be.michielve.f1_api.repositories.DriverRepository;
import be.michielve.f1_api.repositories.PredictionRepository;
import be.michielve.f1_api.repositories.RaceRepository;
import be.michielve.f1_api.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Prediction createPrediction(UUID userId, CreatePredictionRequest request) {
        UUID nonNullUserId = Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(request, "Request cannot be null");
        UUID raceId = Objects.requireNonNull(request.getRaceId(), "Race ID cannot be null");

        List<UUID> predictedDriverIds = request.getPredictedDrivers();

        if (predictedDriverIds == null || predictedDriverIds.size() != 10) {
            throw new IllegalArgumentException("You must provide exactly 10 unique driver IDs.");
        }

        Set<UUID> uniqueDriverIds = new HashSet<>(predictedDriverIds);
        if (uniqueDriverIds.size() != 10) {
            List<UUID> duplicates = findDuplicates(predictedDriverIds);
            throw new IllegalArgumentException("Duplicate driver IDs found: " + duplicates);
        }

        List<UUID> existingDriverIds = driverRepository.findAllById(uniqueDriverIds)
                .stream().map(Driver::getId).toList();

        List<UUID> missingDriverIds = predictedDriverIds.stream()
                .filter(id -> !existingDriverIds.contains(id))
                .toList();

        if (!missingDriverIds.isEmpty()) {
            throw new IllegalArgumentException("Driver(s) not found: " + missingDriverIds);
        }

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

        if (prediction == null) {
            throw new IllegalStateException("Failed to create prediction object");
        }

        try {
            Prediction saved = predictionRepository.save(prediction);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Could not save prediction to database: " + e.getMessage(), e);
        }
    }

    private List<UUID> findDuplicates(List<UUID> list) {
        Set<UUID> seen = new HashSet<>();
        return list.stream()
                .filter(id -> !seen.add(id))
                .distinct()
                .toList();
    }

    private String convertListToJsonString(List<UUID> driverIds) {
        try {
            return objectMapper.writeValueAsString(driverIds);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting list of UUIDs to JSON string", e);
        }
    }
}