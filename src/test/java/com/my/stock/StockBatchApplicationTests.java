package com.my.stock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.flyway.enabled=false")
class StockBatchApplicationTests {

    @Test
    void contextLoads() {
    }

}
