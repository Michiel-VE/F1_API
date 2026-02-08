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
    private static final ConfigurableApplicationContext context;
    private static final F1Scheduler scheduler;

    static {
        try {
            DotenvInitializer.init();

            // For background tasks, we override the 'servlet' setting to 'none'
            context = new SpringApplicationBuilder(F1ApiApplication.class)
                    .web(WebApplicationType.NONE)
                    .run();

            scheduler = context.getBean(F1Scheduler.class);
            
            logger.info("StandingUpdateLambdaHandler initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize StandingUpdateLambdaHandler", e);
            throw new RuntimeException("Could not initialize Spring context", e);
        }
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        logger.info("Starting manual Drivers and Points update...");
        try {
            scheduler.updateCurrentDriversAndPoints();
            return "Successfully updated drivers.";
        } catch (Exception e) {
            logger.error("Error during standing update: {}", e.getMessage(), e);
            return "Failed to update drivers points: " + e.getMessage();
        }
    }
}