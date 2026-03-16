package com.communityhelp.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Define un cliente HTTP reutilizable para consumir APIs externas
 * - Como OpenRouteService.
 */
@Configuration
public class HttpConfig {

    @Value("${openroute.api.base-url}")
    private String apiBaseUrl;

    /**
     * RestClient específico para OpenRouteService.
     */
    @Bean
    public RestClient openRouteRestClient() {
        return RestClient.builder()
                .baseUrl(apiBaseUrl)
                .requestFactory(openRouteRequestFactory())
                .build();
    }

    private ClientHttpRequestFactory openRouteRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        return factory;
    }
}
