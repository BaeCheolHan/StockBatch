package com.my.stock.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients("com.my.stock.api")
public class OpenFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 20, TimeUnit.SECONDS, true);
    }
}
