package com.gdg.sprint.team1.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gdg.sprint.team1.security.CurrentUserArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final List<String> LOCALHOST_PATTERNS = List.of(
        "http://localhost:*",
        "http://127.0.0.1:*"
    );

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsConfig;

    public WebMvcConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Stream.concat(
            LOCALHOST_PATTERNS.stream(),
            Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
        ).collect(Collectors.toList());

        registry.addMapping("/api/**")
            .allowedOriginPatterns(origins.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
