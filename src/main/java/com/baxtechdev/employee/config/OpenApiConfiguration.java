package com.baxtechdev.employee.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the OpenAPI/Swagger configuration for the employee service REST endpoints.
 * Biztosítja az OpenAPI/Swagger konfigurációt az alkalmazottakat kiszolgáló REST végpontokhoz.
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Creates the reusable OpenAPI definition so Swagger UI can describe the REST endpoints.
     * Létrehozza az újrafelhasználható OpenAPI definíciót, így a Swagger UI le tudja írni a REST végpontokat.
     *
     * @return configured OpenAPI model instance
     */
    @Bean
    public OpenAPI employeeServiceOpenAPI() {
        // Provide the essential metadata for the service, improving discoverability in the Swagger UI.
        // Megadja a szolgáltatás alapvető metaadatait, javítva a felfedezhetőséget a Swagger UI felületén.
        return new OpenAPI()
            .info(new Info()
                .title("Employee Service API / Dolgozói szolgáltatás API")
                .version("1.0.0")
                .description("REST endpoints for listing employees and departments. / REST végpontok a dolgozók és osztályok listázásához."));
    }
}
