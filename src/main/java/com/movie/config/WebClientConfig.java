package com.movie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${omdb.api.url}")
    private String omdbUrl;

    @Bean
    public WebClient omdbWebClient() {
        return WebClient.builder()
                .baseUrl(omdbUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}