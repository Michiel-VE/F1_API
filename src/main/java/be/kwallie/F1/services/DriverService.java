package be.kwallie.F1.services;

import be.kwallie.F1.models.response.DriverResponse;
import be.kwallie.F1.models.response.DriverWithTeamResponse;

import java.util.List;

public interface DriverService {
    List<DriverResponse> getAllDrivers();
    DriverResponse getDriver(Long id);
    DriverWithTeamResponse getDriverWithTeam(Long id);
}
