package eu.tib.ontologyhistory.configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI userApi() {
        return new OpenAPI()
                .info(new Info().title("Semantic history of ontology API")
                        .description("API paths used in semantic ontology tool")
                        .version("v0.0.1"));
    }
}


