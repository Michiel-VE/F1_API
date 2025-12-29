package be.michielve.f1_api.lambdas;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.services.F1Scheduler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class StandingUpdateLambdaHandler implements RequestHandler<Object, String> {

    private F1Scheduler scheduler;

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        if (scheduler == null) {
            try {
                // Initialize Spring context dynamically per request
                DotenvInitializer.init();
                ConfigurableApplicationContext context = SpringApplication.run(be.michielve.f1_api.F1ApiApplication.class);
                scheduler = context.getBean(F1Scheduler.class);
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed to initialize context: " + e.getMessage();
            }
        }

        try {
            scheduler.updateCurrentDriversAndPoints();
            return "Successfully updated drivers.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to update drivers points: " + e.getMessage();
        }
    }
}
