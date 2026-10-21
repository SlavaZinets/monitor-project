package com.yoy.monitorproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MonitorProjectApplicationTests {

    @Test
    void contextLoads() {
    }

}
