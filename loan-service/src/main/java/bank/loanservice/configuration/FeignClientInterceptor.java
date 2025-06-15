package bank.loanservice.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
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
                    log.info("Feign token: {}", token);
                } else {
                    System.err.println("[Interceptor Warning] Token generation failed. No Authorization header will be set.");
                }
            }
        };
    }
}
