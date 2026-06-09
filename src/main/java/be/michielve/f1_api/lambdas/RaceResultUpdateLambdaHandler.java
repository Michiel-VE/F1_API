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

public class RaceResultUpdateLambdaHandler implements RequestHandler<Object, String> {

    private static final Logger logger = LoggerFactory.getLogger(RaceResultUpdateLambdaHandler.class);

    static {
        DotenvInitializer.init();
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        logger.info("Starting manual update for Race Results...");
        
        System.setProperty("spring.main.web-application-type", "none");
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(F1ApiApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
            
            F1Scheduler scheduler = context.getBean(F1Scheduler.class);
            scheduler.updateRaceResults();
            return "Successfully updated race results.";
        } catch (Exception e) {
            logger.error("Error during Race Result update: {}", e.getMessage(), e);
            return "Failed to update: " + e.getMessage();
        }
    }
}