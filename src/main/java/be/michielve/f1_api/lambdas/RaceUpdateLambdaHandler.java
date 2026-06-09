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

public class RaceUpdateLambdaHandler implements RequestHandler<Object, String> {

    private static final Logger logger = LoggerFactory.getLogger(RaceUpdateLambdaHandler.class);

    static {
        DotenvInitializer.init();
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        logger.info("Starting manual Race Season update...");
        
        System.setProperty("spring.main.web-application-type", "none");
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(F1ApiApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
            
            F1Scheduler f1Scheduler = context.getBean(F1Scheduler.class);
            f1Scheduler.updateRacesSeason();
            return "Successfully updated races.";
        } catch (Exception e) {
            logger.error("Error during race update: {}", e.getMessage(), e);
            return "Failed to update races: " + e.getMessage();
        }
    }
}