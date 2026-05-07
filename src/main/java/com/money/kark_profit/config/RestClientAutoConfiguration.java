package com.money.kark_profit.config;

import com.money.kark_profit.config.properties.RestClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
public class RestClientAutoConfiguration {
}