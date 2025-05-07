package com.ondoproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("docker")
public class OpenApiConfig {

    @Bean
    public OpenAPI dockerOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("https://backend.ondo-project.com").description("Production Server")));
    }
}