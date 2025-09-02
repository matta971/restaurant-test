package com.restaurant.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API Gateway
 * Provides centralized API documentation
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Reservation System API")
                        .description("""
                            Complete microservices-based restaurant reservation system
                            
                            ## Features
                            - **Restaurant Management**: CRUD operations for restaurants and tables
                            - **Reservation Management**: Complete reservation lifecycle
                            - **JWT Authentication**: Secure API access
                            - **HATEOAS**: Hypermedia-driven REST APIs
                            - **Load Balancing**: High availability and scalability
                            
                            ## Architecture
                            - **API Gateway**: Single entry point with JWT authentication
                            - **Restaurant Service**: Restaurant and table management
                            - **Reservation Service**: Reservation operations
                            - **Load Balancer**: Nginx reverse proxy
                            
                            ## Authentication
                            Use the `/auth/login` endpoint to obtain a JWT token, then include it in the Authorization header:
                            ```
                            Authorization: Bearer <your-jwt-token>
                            ```
                            
                            ## Test Credentials
                            - **Admin**: username: `admin`, password: `admin123`
                            - **Customer**: username: `customer`, password: `customer123`
                            - **Restaurant**: username: `restaurant`, password: `restaurant123`
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Restaurant System Team")
                                .email("contact@restaurant-system.com")
                                .url("https://github.com/your-org/restaurant-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("API Gateway (Development)"),
                        new Server()
                                .url("http://localhost")
                                .description("Load Balancer (Production)"),
                        new Server()
                                .url("https://api.restaurant-system.com")
                                .description("Production Environment")))
                
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme")));
    }
}