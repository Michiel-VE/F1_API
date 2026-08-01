package be.michielve.f1_api;

import org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(F1ApiApplication.FlywayReflectionHints.class)
public class F1ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(F1ApiApplication.class, args);
    }

    static class FlywayReflectionHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                PostgreSQLConfigurationExtension.class,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.ACCESS_PUBLIC_FIELDS,
                MemberCategory.ACCESS_DECLARED_FIELDS
            );
        }
    }
}