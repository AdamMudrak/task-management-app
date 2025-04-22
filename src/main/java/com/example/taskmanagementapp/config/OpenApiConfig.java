package com.example.taskmanagementapp.config;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.SERVER_PATH;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Value(SERVER_PATH) private String serverPath;

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI().addServersItem(new Server().url(serverPath));
    }
}
