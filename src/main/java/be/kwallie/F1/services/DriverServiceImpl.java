package be.kwallie.F1.services;


import be.kwallie.F1.convertors.DriverJsonConverter;
import be.kwallie.F1.convertors.DriverWithTeamJsonConverter;
import be.kwallie.F1.models.Driver;
import be.kwallie.F1.models.request.DriverRequest;
import be.kwallie.F1.models.response.DriverResponse;
import be.kwallie.F1.models.response.DriverWithTeamResponse;
import be.kwallie.F1.repository.DriverRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final DriverJsonConverter driverJsonConverter;
    private final DriverWithTeamJsonConverter driverWithTeamJsonConverter;

    @Override
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAllByOrderByPointsDescLastNameAsc().stream()
                .map(driverJsonConverter::driverResponseConvert)
                .collect(Collectors.toList());
    }

    @Override
    public DriverResponse getDriver(Long id){
        Driver driver = driverRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + id));

        return driverJsonConverter.driverResponseConvert(driver);
    }
    @Override
    public DriverWithTeamResponse getDriverWithTeam(Long id){
        Driver driver = driverRepository
                .findDriverAndTeam(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with Driver id: " + id));

        return driverWithTeamJsonConverter.driverWithTeamResponseConvert(driver);
    }

    @Override
    public DriverResponse editDriver(DriverRequest driverRequest, Long id){
        Driver updateDriver = driverRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        Driver driverUpdated = driverJsonConverter.driverModelConvert(updateDriver, driverRequest);
        driverRepository.save(driverUpdated);
        return driverJsonConverter.driverResponseConvert(driverUpdated);
    }

}
