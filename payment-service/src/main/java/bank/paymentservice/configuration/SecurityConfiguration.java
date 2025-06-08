package bank.paymentservice.configuration;

import io.github.oguzalpcepni.security.configuration.BaseSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final BaseSecurityService baseSecurityService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http = baseSecurityService.configureCoreSecurity(http);

        http.authorizeHttpRequests(auth -> auth
                // Allow unauthenticated access to mock payment endpoint
                .requestMatchers(HttpMethod.POST, "/api/v1/payment/pay")
                .permitAll()

                // Restrict bill payments to customers and admins
                .requestMatchers(HttpMethod.POST, "/api/v1/payment/payBill")
                .hasAnyAuthority("CUSTOMER", "ADMIN")

                // Secure all other endpoints
                .anyRequest().authenticated()
            );


        return http.build();

    }


}