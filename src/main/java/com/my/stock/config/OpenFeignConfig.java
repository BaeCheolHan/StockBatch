package com.my.stock.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.my.stock.api")
public class OpenFeignConfig {
}
