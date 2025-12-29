package be.michielve.f1_api.utils;

import be.michielve.f1_api.models.response.TeamResponse;

import java.util.List;
import java.util.UUID;

/**
 * Helper class to generate mock data for testing purposes.
 */
public class TestTeamDataHelper {

    /**
     * Creates a single mock TeamResponse object.
     *
     * @return A mock TeamResponse instance.
     */
    public static TeamResponse createMockTeamResponse() {
        return TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Red Bull Racing")
                .country("UK")
                .build();
    }

    /**
     * Creates a list of mock TeamResponse objects.
     *
     * @return A list of two mock TeamResponse instances.
     */
    public static List<TeamResponse> createMockTeamResponseList() {
        TeamResponse redBull = createMockTeamResponse();

        TeamResponse mercedes = TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Mercedes")
                .country("UK")
                .build();

        return List.of(redBull, mercedes);
    }
}
