package be.michielve.f1_api.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotenvInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DotenvInitializer.class);

    public static void init() {
        String env = System.getenv("ENV");

        if ("prod".equalsIgnoreCase(env)) {
            loadFromSecretsManager();
        } else {
            loadFromDotenv();
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

        if (dbSecretName == null || dbSecretName.isBlank()) {
            throw new RuntimeException("Missing DB_SECRET_NAME environment variable");
        }
        if (googleSecretName == null || googleSecretName.isBlank()) {
            throw new RuntimeException("Missing GOOGLE_SECRET_NAME environment variable");
        }

        System.out.println("Loading secrets from AWS Secrets Manager...");

        Regions region = Regions.EU_NORTH_1;

        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        System.out.println("Fetching database credentials from AWS Secrets Manager...");

        GetSecretValueRequest dbRequest = new GetSecretValueRequest()
                .withSecretId(dbSecretName);

        try {
            GetSecretValueResult dbResult = client.getSecretValue(dbRequest);
            String dbSecretString = dbResult.getSecretString();
            JSONObject dbJson = new JSONObject(dbSecretString);

            System.setProperty("SPRING_DATASOURCE_USERNAME", dbJson.getString("username"));
            System.setProperty("SPRING_DATASOURCE_PASSWORD", dbJson.getString("password"));
            System.setProperty("SPRING_DATASOURCE_URL", dbJson.getString("url"));

        } catch (Exception e) {
            logger.error("Failed to load DB credentials from AWS Secrets Manager: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to load DB credentials from AWS Secrets Manager", e);
        }

        System.out.println("Fetching Google OAuth credentials and JWT secret from AWS Secrets Manager...");

        GetSecretValueRequest googleRequest = new GetSecretValueRequest()
                .withSecretId(googleSecretName);

        try {
            GetSecretValueResult googleResult = client.getSecretValue(googleRequest);
            String googleSecretString = googleResult.getSecretString();
            JSONObject googleJson = new JSONObject(googleSecretString);

            System.setProperty("GOOGLE_CLIENT_ID", googleJson.getString("google_client_id"));
            System.setProperty("GOOGLE_CLIENT_SECRET", googleJson.getString("google_client_secret"));
            System.setProperty("JWT_SECRET_KEY", googleJson.getString("jwt_secret_key"));

            System.out.println("Google secrets loaded successfully.");

        } catch (Exception e) {
            logger.error("Failed to load Google OAuth credentials and JWT secret from AWS Secrets Manager: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to load Google OAuth credentials and JWT secret from AWS Secrets Manager", e);
        }

        System.out.println("Secrets loading process completed.");
    }
}
