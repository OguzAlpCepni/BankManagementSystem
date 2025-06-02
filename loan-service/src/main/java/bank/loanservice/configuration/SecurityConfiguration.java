package bank.loanservice.configuration;

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
        // özel outhorize işlemleri burada yönet

        http.authorizeHttpRequests(auth -> auth
                // Kredi skoru sorgulama - sadece müşteri kendi skorunu, çalışan/yönetici tüm skorları görebilir
                .requestMatchers(HttpMethod.GET, "/api/v1/loan-application/credit-score/{customerId}")
                .permitAll()

                // Yeni kredi başvurusu oluşturma - sadece müşteriler
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-application")
                .hasAnyAuthority("CUSTOMER","ADMIN")

                // Krediyi onaylayıp para transferi yapma - sadece çalışan ve yöneticiler
                .requestMatchers(HttpMethod.POST, "/api/v1/loan-application/{loanId}/transfer")
                .hasAnyAuthority("CUSTOMER","EMPLOYEE", "ADMIN")

                // Diğer tüm endpointler için kimlik doğrulama gerekli
                .anyRequest().authenticated()



        );


        return http.build();
    }

}
