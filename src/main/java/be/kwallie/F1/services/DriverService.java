package be.kwallie.F1.services;

import be.kwallie.F1.models.response.DriverResponse;

import java.util.List;

public interface DriverService {
    List<DriverResponse> getAllDrivers();
    List<DriverResponse> getTopThree();
    DriverResponse getDriver(Long id);
}
