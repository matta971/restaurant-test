package com.restaurant.service.restaurant.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Restaurant Service
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI restaurantServiceOpenAPI() {
        var devServer = new Server();
        devServer.setUrl("http://localhost:" + serverPort);
        devServer.setDescription("Development server");

        var gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8080/api");
        gatewayServer.setDescription("API Gateway");

        var contact = new Contact();
        contact.setEmail("contact@restaurant-service.com");
        contact.setName("Restaurant Service Team");

        var license = new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT");

        var info = new Info()
            .title("Restaurant Service API")
            .version("1.0.0")
            .contact(contact)
            .description("REST API for managing restaurants, tables, and availability in the restaurant reservation system. " +
                        "This service provides operations for restaurant management, table management, and availability checking.")
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer, gatewayServer));
    }
}