package be.michielve.f1_api.lambdas;

import be.michielve.f1_api.F1ApiApplication;
import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.services.F1Scheduler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class StandingUpdateLambdaHandler implements RequestHandler<Object, String> {

    private static final Logger logger = LoggerFactory.getLogger(StandingUpdateLambdaHandler.class);

    static {
        DotenvInitializer.init();
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        logger.info("Starting manual Drivers and Points update...");
        
        System.setProperty("spring.main.web-application-type", "none");
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(F1ApiApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {

            F1Scheduler scheduler = context.getBean(F1Scheduler.class);
            scheduler.updateCurrentDriversAndPoints();
            return "Successfully updated drivers.";
        } catch (Exception e) {
            logger.error("Error during standing update: {}", e.getMessage(), e);
            return "Failed to update drivers points: " + e.getMessage();
        }
    }
}