package com.banquito.paymentprocessor.validamarca.banquito.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server()
                .url("http://localhost:8083")
                .description("Servidor de Desarrollo");

        Contact contact = new Contact()
                .name("Banquito")
                .url("https://banquito.com")
                .email("info@banquito.com");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("API de Validación de Marca")
                .version("1.0")
                .contact(contact)
                .description("API para la validación y gestión de marcas de tarjetas en el sistema de pagos")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
} 