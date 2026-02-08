package be.michielve.f1_api.controllers;

import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;
import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class DriverController {

        private final DriverService driverService;

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
        public ResponseEntity<DriverResponse> getDriverDetails(
                        @RequestParam("permanentNumber") int permanentNumber) {
                DriverResponse driver = driverService.getDriverDetails(permanentNumber);

                if (driver == null) {
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(driver);
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

        // TODO: Create drivers/name te get history of the driver
}
