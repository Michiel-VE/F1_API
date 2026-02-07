package be.michielve.f1_api.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotenvInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DotenvInitializer.class);
    private static volatile boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        synchronized (DotenvInitializer.class) {
            if (initialized) {
                return;
            }

            // Force Spring to treat this as a Servlet Web App before startup
            // This prevents the ClassCastException in AWS Lambda
            System.setProperty("spring.main.web-application-type", "servlet");

            String env = System.getenv("ENV");

            if ("prod".equalsIgnoreCase(env)) {
                loadFromSecretsManager();
            } else {
                loadFromDotenv();
            }

            initialized = true;
        }
    }

    private static void loadFromDotenv() {
        System.out.println("Loading environment variables from local .env file...");

        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources/environments")
                .filename("local.env")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        System.out.println("Environment variables loaded from .env file successfully.");
    }

    private static void loadFromSecretsManager() {
        String dbSecretName = System.getenv("DB_SECRET_NAME");
        String googleSecretName = System.getenv("GOOGLE_SECRET_NAME");

        if (dbSecretName == null || dbSecretName.isBlank() || 
            googleSecretName == null || googleSecretName.isBlank()) {
            throw new RuntimeException("Missing Secret Name environment variables (DB_SECRET_NAME or GOOGLE_SECRET_NAME)");
        }

        System.out.println("Loading secrets from AWS Secrets Manager (v2 SDK)...");

        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.EU_NORTH_1)
                .build()) {

            // 1. Fetch Database Secrets
            GetSecretValueResponse dbResponse = client.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(dbSecretName)
                    .build());
            JSONObject dbJson = new JSONObject(dbResponse.secretString());

            System.setProperty("spring.datasource.url", dbJson.getString("url"));
            System.setProperty("spring.datasource.username", dbJson.getString("username"));
            System.setProperty("spring.datasource.password", dbJson.getString("password"));
            
            // Explicitly disable Flyway for Production/AWS
            System.setProperty("spring.flyway.enabled", "false");

            // 2. Fetch Google & JWT Secrets
            GetSecretValueResponse googleResponse = client.getSecretValue(GetSecretValueRequest.builder()
                    .secretId(googleSecretName)
                    .build());
            JSONObject googleJson = new JSONObject(googleResponse.secretString());

            // --- MANDATORY FOR SPRING SECURITY OAUTH2 ---
            System.setProperty("spring.security.oauth2.client.registration.google.client-id", googleJson.getString("google_client_id"));
            System.setProperty("spring.security.oauth2.client.registration.google.client-secret", googleJson.getString("google_client_secret"));
            
            // --- CUSTOM APP KEYS ---
            System.setProperty("google.client.id", googleJson.getString("google_client_id"));
            System.setProperty("google.client.secret", googleJson.getString("google_client_secret"));
            System.setProperty("jwt.secret.key", googleJson.getString("jwt_secret_key"));

            String expiration = googleJson.has("jwt_expiration") ? googleJson.getString("jwt_expiration") : "86400000";
            System.setProperty("jwt.expiration", expiration);

            System.out.println("Secrets loading and Spring Security property mapping completed successfully.");

        } catch (Exception e) {
            logger.error("Failed to load secrets from AWS Secrets Manager: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to load secrets from AWS", e);
        }
    }
}