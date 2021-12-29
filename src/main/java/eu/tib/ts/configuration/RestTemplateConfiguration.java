package eu.tib.ts.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Value("${ts.timeout.connect}")
    private int connectTimeout;
    @Value("${ts.timeout.read}")
    private int readTimeout;

    @Autowired
    private MappingJackson2HttpMessageConverter messageConverter;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
            .setConnectTimeout(Duration.ofMillis(connectTimeout))
            .setReadTimeout(Duration.ofMillis(readTimeout))
            .additionalMessageConverters(List.of(messageConverter))
            .build();
    }
}
