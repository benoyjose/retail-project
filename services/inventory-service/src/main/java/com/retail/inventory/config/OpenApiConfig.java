package com.retail.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Inventory Microservice API")
                        .description("REST API documentation for Inventory Management and Stock Reservation (Use Cases 7 & 4)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Retail Engineering Team")
                                .email("engineering@retail.example.com")));
    }
}

