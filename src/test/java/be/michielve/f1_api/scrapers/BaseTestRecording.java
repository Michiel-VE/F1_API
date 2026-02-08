package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class BaseTestRecording {

    @RegisterExtension
    protected static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig()
                .port(8080)
                .usingFilesUnderDirectory("src/test/resources/wiremock"))
            .build();

    @BeforeAll
    static void setupBase() {
        DotenvInitializer.init();
    }

    /**
     * @param urlPattern The regex or glob for the URL to stub/proxy
     * @param testLogic  The lambda containing the service call
     */
    protected void runWithRecording(String urlPattern, Runnable testLogic) {
        // Fallback proxy stub with low priority (checks local files first)
        wm.stubFor(get(urlMatching(urlPattern))
                .atPriority(10)
                .willReturn(aResponse()
                        .proxiedFrom("https://www.formula1.com")));

        testLogic.run();

        // Check if the record flag is passed via Gradle (-Dwiremock.record=true)
        if ("true".equals(System.getProperty("wiremock.record"))) {
            wm.snapshotRecord(recordSpec()
                    .forTarget("https://www.formula1.com")
                    .makeStubsPersistent(true)
                    .extractTextBodiesOver(0));
        }
    }
}