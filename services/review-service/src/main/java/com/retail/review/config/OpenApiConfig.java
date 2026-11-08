package com.retail.review.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reviewServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Review Microservice API")
                        .description("REST API documentation for Product Reviews, Ratings & Verified Purchase Checks (Use Case 6)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Retail Engineering Team")
                                .email("engineering@retail.example.com")));
    }
}

