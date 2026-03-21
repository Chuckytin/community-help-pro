package com.communityhelp.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para la documentación de la API.
 * Define la información general de la API y la configuración de seguridad JWT.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI communityHelpOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Community Help API")
                        .description("""
                                Local solidarity platform API.
                                Connects people who want to donate goods or request specific help with nearby volunteers.
                                
                                **Main flow:**
                                1. Register with /auth/register and get your JWT with /auth/login
                                2. Use the token in the Authorize button (top right)
                                3. Create a HelpRequest or Donation → the system automatically generates proposals
                                4. A volunteer accepts the proposal → it gets assigned and the chat opens
                                """)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("Community Help")
                                .url("https://github.com/Chuckytin/community-help-pro")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter the JWT token obtained from /api/v1/auth/login"))
                        .addResponses("400", new ApiResponse().description("Invalid data or validation failed"))
                        .addResponses("401", new ApiResponse().description("JWT token missing or invalid"))
                        .addResponses("403", new ApiResponse().description("Insufficient permissions for this operation"))
                        .addResponses("404", new ApiResponse().description("Entity not found"))
                        .addResponses("409", new ApiResponse().description("State conflict or duplicate"))
                        .addResponses("500", new ApiResponse().description("Internal server error")));
    }
}