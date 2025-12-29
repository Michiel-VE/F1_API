package be.michielve.f1_api.utils;

import be.michielve.f1_api.models.response.TeamSeasonResponse;

import java.util.List;

public class TeamSeasonResponseDataHelper {

    /**
     * Creates a single mock TeamSeasonResponse object.
     *
     * @return A mock TeamSeasonResponse instance.
     */
    public static TeamSeasonResponse createMockTeamSeasonResponse() {
        return TeamSeasonResponse.builder()
                .teamName("Mercedes")
                .seasonName("2021")
                .points(613)
                .build();
    }

    /**
     * Creates a list of mock TeamSeasonResponse objects.
     * This list is based on the data provided in the DriverWithSeasonsResponse helper.
     *
     * @return A list of mock TeamSeasonResponse instances.
     */
    public static List<TeamSeasonResponse> createMockTeamSeasonResponseList() {
        return List.of(
                TeamSeasonResponse.builder().teamName("Minardi").seasonName("2001").points(0).build(),
                TeamSeasonResponse.builder().teamName("Renault").seasonName("2003").points(55).build(),
                TeamSeasonResponse.builder().teamName("Renault").seasonName("2005").points(133).build(),
                TeamSeasonResponse.builder().teamName("Ferrari").seasonName("2010").points(252).build(),
                TeamSeasonResponse.builder().teamName("Alpine").seasonName("2021").points(81).build()
        );
    }
}
