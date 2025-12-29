package be.michielve.f1_api.scrapers;

import be.michielve.f1_api.config.DotenvInitializer;
import be.michielve.f1_api.services.F1Scheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DriverPointsTest {

    @Autowired
    private F1Scheduler f1Scheduler;



    @BeforeAll
    static void setVars(){
        DotenvInitializer.init();
    }

    @Test
    public void testDriverPointsUpdate() {
        f1Scheduler.updateCurrentDriversAndPoints();

        assert(true);
    }
}
