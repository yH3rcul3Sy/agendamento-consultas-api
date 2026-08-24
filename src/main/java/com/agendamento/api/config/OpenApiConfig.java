package com.agendamento.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI disponivel em http://localhost:8080/swagger-ui.html apos rodar a aplicacao.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Sistema de Agendamento de Consultas",
                version = "1.0.0",
                description = "API para agendamento, cancelamento e controle de horarios de consultas, com notificacao por e-mail (AWS SES).",
                contact = @Contact(name = "Projeto de Portfolio")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
