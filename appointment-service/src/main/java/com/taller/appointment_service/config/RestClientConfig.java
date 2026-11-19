package com.taller.appointment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient mechanicRestClient(
            @Value("${mechanic-service.url}") String baseUrl,
            @Value("${mechanic-service.username}") String username,
            @Value("${mechanic-service.password}") String password) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }
}