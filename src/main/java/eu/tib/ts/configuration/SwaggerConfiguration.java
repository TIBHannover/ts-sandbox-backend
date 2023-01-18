package eu.tib.ts.configuration;

import io.swagger.annotations.ApiParam;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("Terminology Service Statistics API")
            .select()
            .paths(PathSelectors.any())
            .apis(RequestHandlerSelectors.basePackage("eu.tib.ts.controller"))
            .build()
            .directModelSubstitute(Pageable.class, SwaggerPageable.class);
    }



    @Getter
    private static class SwaggerPageable {

        @ApiParam(value = "Number of records per page", example = "0")
        private Integer size;

        @ApiParam(value = "Results page you want to retrieve (0..N)", example = "0")
        private Integer page;

        @ApiParam(value = "Sorting criteria in the format: property(,asc|desc). " +
            "Default sort order is ascending. Multiple sort criteria are supported." +
            "Example: sort=name,asc&sort=age,desc")
        private String sort;

    }
}


