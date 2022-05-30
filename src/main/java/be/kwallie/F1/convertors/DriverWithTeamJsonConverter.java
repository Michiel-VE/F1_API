package be.kwallie.F1.convertors;

import be.kwallie.F1.models.Driver;
import be.kwallie.F1.models.response.DriverWithTeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverWithTeamJsonConverter {
    public DriverWithTeamResponse driverWithTeamResponseConvert(Driver driver){
        return DriverWithTeamResponse.builder()
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
                .teamPicture(driver.getTeam().getPicture())
                .teamName(driver.getTeam().getName())
                .build();
    }
}
