package com.huydevcorn.todo_app.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger.
 */
@Configuration
public class SwaggerConfig {
    /**
     * Configures and returns an OpenAPI bean for Swagger documentation.
     *
     * @return the configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API Documentation for To-do app")
                        .version("1.0")
                        .description("API Documentation for To-do app"));
    }
}
