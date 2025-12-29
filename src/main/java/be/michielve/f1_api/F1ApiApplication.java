package be.michielve.f1_api;

import be.michielve.f1_api.config.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class F1ApiApplication {

    public static void main(String[] args) {
        DotenvInitializer.init();

        SpringApplication app = new SpringApplication(F1ApiApplication.class);
        app.setDefaultProperties(System.getProperties());
        app.run(args);
    }
}
