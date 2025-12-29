package be.michielve.f1_api.utils;

import be.michielve.f1_api.models.response.DriverResponse;
import be.michielve.f1_api.models.response.DriverWithSeasonsResponse;
import be.michielve.f1_api.models.response.DriverCareerHistoryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Helper class to generate mock data for testing purposes.
 * This prevents tests from being dependent on a live database or complex data setup.
 */
public class TestDriverDataHelper {

    /**
     * Creates a single mock DriverResponse object.
     *
     * @return A mock DriverResponse instance.
     */
    public static DriverResponse createMockDriverResponse() {
        return DriverResponse.builder()
                .driverId(UUID.randomUUID())
                .permanentNumber(33)
                .firstname("Max")
                .lastname("Verstappen")
                .birthday(LocalDate.parse("1997-09-30"))
                .country("Netherlands")
                .build();
    }

    /**
     * Creates a list of mock DriverResponse objects.
     *
     * @return A list of two mock DriverResponse instances.
     */
    public static List<DriverResponse> createMockDriverResponseList() {
        DriverResponse verstappen = createMockDriverResponse();

        DriverResponse hamilton = DriverResponse.builder()
                .driverId(UUID.randomUUID())
                .permanentNumber(44)
                .firstname("Lewis")
                .lastname("Hamilton")
                .birthday(LocalDate.parse("1985-01-07"))
                .country("Great Britain")
                .build();

        return List.of(verstappen, hamilton);
    }

    /**
     * Creates a single mock DriverWithSeasonsResponse object.
     *
     * @return A mock DriverWithSeasonsResponse instance.
     */
    public static DriverWithSeasonsResponse createMockDriverWithSeasonsResponse() {
        return DriverWithSeasonsResponse.builder()
                .driverId(UUID.randomUUID())
                .firstname("Charles")
                .lastname("Leclerc")
                .birthday(LocalDate.parse("1997-10-16"))
                .country("Monaco")
                .teamSeasons(TeamSeasonResponseDataHelper.createMockTeamSeasonResponseList())
                .build();
    }

    /**
     * Creates a list of mock DriverWithSeasonsResponse objects.
     *
     * @return A list of two mock DriverWithSeasonsResponse instances.
     */
    public static List<DriverWithSeasonsResponse> createMockDriverWithSeasonsResponseList() {
        DriverWithSeasonsResponse leclerc = createMockDriverWithSeasonsResponse();

        DriverWithSeasonsResponse alonso = DriverWithSeasonsResponse.builder()
                .driverId(UUID.randomUUID())
                .firstname("Fernando")
                .lastname("Alonso")
                .birthday(LocalDate.parse("1981-07-29"))
                .country("Spain")
                .teamSeasons(TeamSeasonResponseDataHelper.createMockTeamSeasonResponseList())
                .build();

        return List.of(leclerc, alonso);
    }

    /**
     * Creates a single mock DriverCareerHistoryResponse object.
     *
     * @return A mock DriverCareerHistoryResponse instance.
     */
    public static DriverCareerHistoryResponse createMockDriverCareerHistoryResponse() {
        return DriverCareerHistoryResponse.builder()
                .team("Mercedes")
                .season("2021")
                .build();
    }

    /**
     * Creates a list of mock DriverCareerHistoryResponse objects.
     *
     * @return A list of two mock DriverCareerHistoryResponse instances.
     */
    public static List<DriverCareerHistoryResponse> createMockDriverCareerHistoryResponseList() {
        DriverCareerHistoryResponse hamilton2021 = DriverCareerHistoryResponse.builder()
                .team("Mercedes")
                .season("2021")
                .build();

        DriverCareerHistoryResponse hamilton2020 = DriverCareerHistoryResponse.builder()
                .team("Mercedes")
                .season("2020")
                .build();

        return List.of(hamilton2021, hamilton2020);
    }
}
