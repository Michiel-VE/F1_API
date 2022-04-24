package be.kwallie.F1.services;


import be.kwallie.F1.convertors.DriverJsonConverter;
import be.kwallie.F1.models.Driver;
import be.kwallie.F1.models.response.DriverResponse;
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

}
