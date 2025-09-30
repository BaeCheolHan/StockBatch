package com.my.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "benchmark")
public class BenchmarkConfigProperties {
    private List<String> codes;
    private History history = new History();
    private Schedule schedule = new Schedule();

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public static class History {
        private String range = "1y";
        private String interval = "1d";
        private boolean autoAdjust = true;

        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public String getInterval() { return interval; }
        public void setInterval(String interval) { this.interval = interval; }
        public boolean isAutoAdjust() { return autoAdjust; }
        public void setAutoAdjust(boolean autoAdjust) { this.autoAdjust = autoAdjust; }
    }

    public static class Schedule {
        private String dailyCron = "0 30 0 * * ?";
        public String getDailyCron() { return dailyCron; }
        public void setDailyCron(String dailyCron) { this.dailyCron = dailyCron; }
    }
}


