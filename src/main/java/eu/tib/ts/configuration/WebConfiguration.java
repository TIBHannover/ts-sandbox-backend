package eu.tib.ts.configuration;

import eu.tib.ts.converter.StringToEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
public class WebConfiguration  implements WebMvcConfigurer  {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEnumConverter());
    }

    /**
     * @author Aamir Muhammad <aamir.muhhamad@tib.eu>
     *     To remove CORS origin issue we created a method that adds cors mappings using cors registry.
     *     WebConfiguration class does not extend WebMvcConfigurerAdapter.
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }


}
