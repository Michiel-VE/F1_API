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
        logger.info("Loading local environment variables...");
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources/environments")
                .filename("local.env")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // Ensure Redis URL is formatted correctly for local if it exists in local.env
        String rawRedisUrl = System.getProperty("UPSTASH_REDIS_URL");
        String redisToken = System.getProperty("UPSTASH_REDIS_TOKEN");
        
        if (rawRedisUrl != null && redisToken != null) {
            String endpoint = rawRedisUrl.replace("https://", "");
            String redisUrl = String.format("rediss://default:%s@%s:%s", redisToken, endpoint, "6379");
            System.setProperty("spring.data.redis.url", redisUrl);
        }
    }

    private static void loadFromSecretsManager() {
        // Reads from single environment variable (e.g. APP_SECRET_NAME="f1-api/secrets")
        String appSecretName = System.getenv("APP_SECRET_NAME");

        if (appSecretName == null || appSecretName.isBlank()) {
            throw new RuntimeException("Missing APP_SECRET_NAME environment variable.");
        }

        try (SecretsManagerClient client = SecretsManagerClient.builder().region(Region.EU_NORTH_1).build()) {

            JSONObject secretsJson = fetchSecret(client, appSecretName);

            // 1. Database Configuration
            String dbUrl = secretsJson.getString("url");
            if (!dbUrl.startsWith("jdbc:")) {
                dbUrl = "jdbc:" + dbUrl;
            }

            System.setProperty("spring.datasource.url", dbUrl);
            System.setProperty("spring.datasource.jdbc-url", dbUrl); // Explicit fallback for Hikari
            System.setProperty("spring.flyway.url", dbUrl);           // Ensures Flyway receives valid JDBC prefix
            System.setProperty("spring.datasource.username", secretsJson.getString("db_username"));
            System.setProperty("spring.datasource.password", secretsJson.getString("db_password"));

            // 2. Google OAuth & JWT Configuration
            System.setProperty("spring.security.oauth2.client.registration.google.client-id",
                    secretsJson.getString("google_client_id"));
            System.setProperty("spring.security.oauth2.client.registration.google.client-secret",
                    secretsJson.getString("google_client_secret"));
            System.setProperty("jwt.secret.key", secretsJson.getString("jwt_secret_key"));

            // 3. Upstash Redis Configuration
            String token = secretsJson.getString("upstash_redis_token");
            String endpoint = secretsJson.getString("upstash_redis_url").replace("https://", "");
            
            // Format required by Spring Data Redis: rediss://default:token@endpoint:port
            String redisUrl = String.format("rediss://default:%s@%s:%s", token, endpoint, "6379");
            System.setProperty("spring.data.redis.url", redisUrl);

            logger.info("All secrets loaded from single AWS Secret ({}) and Spring properties mapped successfully.", appSecretName);
        } catch (Exception e) {
            logger.error("Failed to load secrets: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to load secrets from AWS", e);
        }
    }

    private static JSONObject fetchSecret(SecretsManagerClient client, String secretId) {
        GetSecretValueResponse response = client
                .getSecretValue(GetSecretValueRequest.builder().secretId(secretId).build());
        return new JSONObject(response.secretString());
    }
}