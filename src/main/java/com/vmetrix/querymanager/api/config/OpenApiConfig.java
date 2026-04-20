package com.vmetrix.querymanager.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Centralised OpenAPI / Swagger configuration.
 *
 * <p>Declares the API metadata (title, version, description, contact, license, servers)
 * exposed at {@code /v3/api-docs} and rendered by the Swagger UI at
 * {@code /swagger-ui.html}.
 *
 * <p>Lives in the {@code api/} layer because Springdoc annotations and beans are
 * HTTP-adapter concerns (see {@code CLAUDE.md} — Layer Dependency Rules).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI queryManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VMetrix Query Manager API")
                        .version("0.1.0")
                        .description(
                                "Metadata-driven SQL query engine for VMetrix.\n\n"
                                        + "Accepts a high-level query specification (columns + filters) and generates "
                                        + "parameterized SQL automatically, resolving JOINs between tables based on "
                                        + "relationship metadata stored in the database.\n\n"
                                        + "Adding a new entity or field requires **zero Java code changes** — "
                                        + "everything is driven by metadata configuration.")
                        .contact(new Contact()
                                .name("VMetrix Engineering")
                                .url("https://vmetrix.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://vmetrix.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README and design decisions")
                        .url("https://github.com/vmetrix/query-manager#readme"))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")))
                .components(new Components());
    }
}
