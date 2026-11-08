package com.retail.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Customer Microservice API")
                        .description("REST API documentation for Customer Registration and Profile Management (Use Case 1)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Retail Engineering Team")
                                .email("engineering@retail.example.com")));
    }
}

