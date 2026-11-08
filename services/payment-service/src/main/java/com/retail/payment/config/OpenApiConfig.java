package com.retail.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Payment Microservice API")
                        .description("REST API documentation for Payment Processing and Transaction Auditing (Use Case 4)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Retail Engineering Team")
                                .email("engineering@retail.example.com")));
    }
}

