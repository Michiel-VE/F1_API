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
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public Prediction createPrediction(UUID userId, CreatePredictionRequest request) {
        try {
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

            if (predictionRepository.existsByUserIdAndRaceId(userId, request.getRaceId())) {
                throw new IllegalArgumentException("You already submitted a prediction for this race.");
            }

            // Fetch the race and user entities
            Race race = raceRepository.findById(request.getRaceId())
                    .orElseThrow(() -> new IllegalArgumentException("Race not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Prediction prediction = Prediction.builder()
                    .user(user)
                    .race(race)
                    .predictedDrivers(convertListToJsonString(predictedDriverIds)) // Convert list to JSON string
                    .build();

            return predictionRepository.save(prediction);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
            return new ObjectMapper().writeValueAsString(driverIds);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting list of UUIDs to JSON string", e);
        }
    }
}
