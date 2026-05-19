package eu.tib.ts.configuration;


import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate(MappingJackson2HttpMessageConverter messageConverter) {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        restTemplate.setMessageConverters(Collections.singletonList(messageConverter));
        
        // Add interceptor to include caller header for request tracking
        List<org.springframework.http.client.ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new CallerHeaderInterceptor());
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setSocketTimeout(Timeout.ofSeconds(5))
                .build();

        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(connectionConfig);

        CloseableHttpClient client = HttpClientBuilder.create()
                .setConnectionManager(cm)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }

}
