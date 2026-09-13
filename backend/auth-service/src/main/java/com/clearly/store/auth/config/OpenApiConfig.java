package com.clearly.store.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI authOpenApi() {
        return new OpenAPI().info(new Info().title("Clearly Store Authentication API").version("1.0.0")
            .description("Customer registration, OTP verification, JWT sessions and Google sign-in"))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
            .tags(List.of(new Tag().name("Account Registration"), new Tag().name("Authentication"), new Tag().name("User Profile"), new Tag().name("Admin Users")));
    }
}
