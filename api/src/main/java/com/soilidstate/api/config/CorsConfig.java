package com.soilidstate.api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for Local Development
 * Allows Angular (localhost:4200), iOS Simulator, and other local clients
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Allow common local development origins
                .allowedOrigins(
                        "http://localhost:4200",           // Angular default
                        "http://localhost:3000",           // React default
                        "http://localhost:8081",           // Alternative port
                        "http://127.0.0.1:4200",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:8081",
                        "capacitor://localhost",           // Capacitor iOS
                        "ionic://localhost"                // Ionic iOS
                )
                .allowedOriginPatterns(
                        "http://192.168.*.*:*",            // Local network
                        "http://10.*.*.*:*",               // Local network
                        "http://172.16.*.*:*"              // Local network
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * Additional CORS filter bean for WebSocket support
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(false);

        // Allowed origins for development
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:3000",
                "http://localhost:8081",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8081"
        ));

        // Allowed origin patterns for local network
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://192.168.*.*:*",
                "http://10.*.*.*:*",
                "http://172.16.*.*:*"
        ));

        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // Allow all methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // Expose headers for client
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

/**
 * Alternative Production-Ready CORS Configuration
 * Uncomment and use this for production deployments
 */
/*
@Configuration
@Profile("production")
public class ProductionCorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
*/