package com.my.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "benchmark")
public class BenchmarkConfigProperties {
    private List<String> codes;
    private History history = new History();
    private Schedule schedule = new Schedule();

    @Setter
    @Getter
    public static class History {
        private String range = "1y";
        private String interval = "1d";
        private boolean autoAdjust = true;

    }

    @Setter
    @Getter
    public static class Schedule {
        private String dailyCron = "0 30 0 * * ?";

    }
}


