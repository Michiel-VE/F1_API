package be.michielve.f1_api.lambdas;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.services.F1Scheduler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class RaceUpdateLambdaHandler implements RequestHandler<Object, String> {

    private static ConfigurableApplicationContext context;
    private static F1Scheduler f1Scheduler;

    static {
        try {
            DotenvInitializer.init();
            context = SpringApplication.run(be.michielve.f1_api.F1ApiApplication.class);
            f1Scheduler = context.getBean(F1Scheduler.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String handleRequest(Object input, Context lambdaContext) {
        try {
            f1Scheduler.updateRacesSeason();
            return "Successfully updated races.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to update races: " + e.getMessage();
        }
    }
}
