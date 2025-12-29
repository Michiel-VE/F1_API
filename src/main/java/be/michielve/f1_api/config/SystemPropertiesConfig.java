package be.michielve.f1_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

@Configuration
public class SystemPropertiesConfig {

    private final ConfigurableEnvironment environment;

    public SystemPropertiesConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void injectSystemProperties() {
        // Add System.getProperties() as the highest priority property source
        Properties systemProps = System.getProperties();
        PropertiesPropertySource pps = new PropertiesPropertySource("systemProps", systemProps);
        environment.getPropertySources().addFirst(pps);
    }
}
