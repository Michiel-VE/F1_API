package be.kwallie.F1.controllers;

import be.kwallie.F1.models.response.DriverResponse;
import be.kwallie.F1.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<DriverResponse>> getTopThree() {
        return ResponseEntity.ok(driverService.getTopThree());
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable Long id){
        return ResponseEntity.ok(driverService.getDriver(id));
    }
}
