package be.michielve.f1_api;

import be.michielve.f1_api.config.DotenvInitializer;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "be.michielve.f1_api.repositories")
@EntityScan(basePackages = "be.michielve.f1_api.models")
@RegisterReflectionForBinding(org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension.class)
public class F1ApiApplication {

    static {
        DotenvInitializer.init();
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(F1ApiApplication.class);
        app.setDefaultProperties(System.getProperties());
        app.run(args);
    }
}