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
        String dbSecretName = System.getenv("DB_SECRET_NAME");
        String googleSecretName = System.getenv("GOOGLE_SECRET_NAME");
        String upstashSecretName = System.getenv("UPSTASH_SECRET_NAME");

        if (dbSecretName == null || googleSecretName == null || upstashSecretName == null) {
            throw new RuntimeException(
                    "Missing Secret Name environment variables (DB_SECRET_NAME, GOOGLE_SECRET_NAME, or UPSTASH_SECRET_NAME)");
        }

        try (SecretsManagerClient client = SecretsManagerClient.builder().region(Region.EU_NORTH_1).build()) {

            // 1. Database
            JSONObject dbJson = fetchSecret(client, dbSecretName);
            System.setProperty("spring.datasource.url", dbJson.getString("url"));
            System.setProperty("spring.datasource.username", dbJson.getString("username"));
            System.setProperty("spring.datasource.password", dbJson.getString("password"));
            System.setProperty("spring.flyway.enabled", "false");

            // 2. Google & JWT
            JSONObject googleJson = fetchSecret(client, googleSecretName);
            System.setProperty("spring.security.oauth2.client.registration.google.client-id",
                    googleJson.getString("google_client_id"));
            System.setProperty("spring.security.oauth2.client.registration.google.client-secret",
                    googleJson.getString("google_client_secret"));
            System.setProperty("jwt.secret.key", googleJson.getString("jwt_secret_key"));

            // 3. Upstash Redis
            JSONObject upstashJson = fetchSecret(client, upstashSecretName);
            String token = upstashJson.getString("upstash_redis_token");
            String endpoint = upstashJson.getString("upstash_redis_url").replace("https://", "");
            
            // Format required by Spring Data Redis: rediss://default:token@endpoint:port
            String redisUrl = String.format("rediss://default:%s@%s:%s", token, endpoint, "6379");
            System.setProperty("spring.data.redis.url", redisUrl);

            logger.info("All secrets loaded and Spring properties mapped successfully.");
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