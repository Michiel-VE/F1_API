package be.kwallie.F1.convertors;


import be.kwallie.F1.models.Driver;
import be.kwallie.F1.models.request.DriverRequest;
import be.kwallie.F1.models.response.DriverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverJsonConverter {
    public Driver driverModelConvert(Driver driver, DriverRequest driverRequest){
        driver.setPoints(driverRequest.getPoints());
        driver.setPenaltyPoints(driverRequest.getPenaltyPoints());
        driver.setWins(driverRequest.getWins());
        return driver;
    }

    public DriverResponse driverResponseConvert(Driver driver){
        return DriverResponse.builder()
                .id(driver.getId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .permanentNumber(driver.getPermanentNumber())
                .code(driver.getCode())
                .birthday(driver.getBirthday())
                .picture(driver.getPicture())
                .country(driver.getCountry())
                .countryCode(driver.getCountryCode())
                .wins(driver.getWins())
                .points(driver.getPoints())
                .penaltyPoints(driver.getPenaltyPoints())
                .build();
    }
}
