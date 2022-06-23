package be.kwallie.F1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"be.kwallie.F1.*"})
public class F1Application {

    public static void main(String[] args) {
        SpringApplication.run(F1Application.class, args);
    }

}
