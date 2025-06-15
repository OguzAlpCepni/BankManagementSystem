package bank.accountservice.configuration;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientInterceptor {

    private final InternalAuthTokenGenerator internalAuthTokenGenerator;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String token = internalAuthTokenGenerator.generateInternalToken();
                if (token != null) {
                    template.header("Authorization", "Bearer " + token);
                } else {
                    System.err.println("[Interceptor Warning] Token generation failed. No Authorization header will be set.");
                }
            }
        };
    }
}