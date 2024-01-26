package eu.tib.tiva.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI userApi(){

        return new OpenAPI()
                .info(new Info().title("Trade in Value Added Statistics API")
                        .description("API paths used in visualization Trade in Value Added data")
                        .version("v0.0.1"));
    }
}