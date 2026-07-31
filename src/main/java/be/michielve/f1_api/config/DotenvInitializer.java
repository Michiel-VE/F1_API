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

            // Disable Flyway execution during GraalVM native image build time
            if (isNativeImageBuildTime()) {
                System.setProperty("spring.flyway.enabled", "false");
            }

            String env = System.getenv("ENV");

            if ("prod".equalsIgnoreCase(env)) {
                loadFromSecretsManager();
            } else {
                loadFromDotenv();
            }

            initialized = true;
        }
    }

    private static boolean isNativeImageBuildTime() {
        return "true".equals(System.getProperty("org.graalvm.nativeimage.imagecode"))
            || System.getenv("GITHUB_ACTIONS") != null;
    }

    private static void loadFromDotenv() {
        logger.info("Loading local environment variables...");
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources/environments")
                .filename("local.env")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        // Safe fallback for Flyway/DataSource in CI if variables are missing
        if (System.getProperty("spring.datasource.url") == null) {
            System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/dummy");
            System.setProperty("spring.datasource.username", "postgres");
            System.setProperty("spring.datasource.password", "postgres");
            System.setProperty("spring.flyway.enabled", "false");
        }

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
            System.setProperty("spring.datasource.jdbc-url", dbUrl);
            System.setProperty("spring.flyway.url", dbUrl);
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