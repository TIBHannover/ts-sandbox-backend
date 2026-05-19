package eu.tib.ts.configuration;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Interceptor to add "caller: MAPPINGS" header to all HTTP requests
 * sent to the TIB Terminology Service API for tracking request origins.
 */
public class CallerHeaderInterceptor implements ClientHttpRequestInterceptor {

    private static final String CALLER_HEADER = "caller";
    private static final String CALLER_VALUE = "MAPPINGS";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(CALLER_HEADER, CALLER_VALUE);
        return execution.execute(request, body);
    }
}
