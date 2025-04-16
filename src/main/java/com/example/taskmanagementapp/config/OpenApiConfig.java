package com.example.taskmanagementapp.config;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.SERVER_PATH;

import com.example.taskmanagementapp.constants.config.ConfigConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Value(SERVER_PATH) private String serverPath;

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(ConfigConstants.SECURITY_SCHEME_KEY,
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(ConfigConstants.SECURITY_SCHEME)
                                .bearerFormat(ConfigConstants.BEARER_FORMAT)))
                                .addSecurityItem(new SecurityRequirement()
                                .addList(ConfigConstants.SECURITY_SCHEME_KEY))
                                .addServersItem(new Server().url(serverPath));
    }
}
