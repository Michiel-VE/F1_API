package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;
import be.michielve.f1_api.models.response.DriverRaceResultsResponse;
import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.services.DriverRaceResultsService;
import be.michielve.f1_api.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class DriverController {

        private final DriverService driverService;
        private final DriverRaceResultsService driverRaceResultsService;

        @GetMapping("drivers")
        public ResponseEntity<List<DriverWithSeasonsResponse>> getAllDrivers() {
                return ResponseEntity.ok(driverService.getAllDrivers());
        }

        @GetMapping("drivers/{season}")
        public ResponseEntity<List<DriverWithSeasonsResponse>> getAllDriversForSeason(
                        @PathVariable String season) {
                List<DriverWithSeasonsResponse> drivers = driverService.getAllDriversForSeason(season);

                if (drivers.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.ok(drivers);
        }

        @GetMapping("driver")
        public ResponseEntity<List<DriverResponse>> getDriverDetails(
                        @RequestParam("permanentNumber") int permanentNumber) {

                List<DriverResponse> drivers = driverService.getDriverDetails(permanentNumber);

                if (drivers.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(drivers);
        }

        @GetMapping("drivers/{name}/career-history")
        public ResponseEntity<List<DriverCareerHistoryResponse>> getDriverCareerHistory(
                        @PathVariable String name) {
                List<DriverCareerHistoryResponse> careerHistory = driverService.getDriverCareerHistory(name);
                if (careerHistory.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(careerHistory);
        }

        @GetMapping("drivers/{id}/race-results")
        public ResponseEntity<DriverRaceResultsResponse> getDriverRaceResults(
                        @PathVariable UUID id,
                        @RequestParam("season") String season) {
                return ResponseEntity.ok(driverRaceResultsService.getDriverRaceResults(id, season));
        }
}