package bank.transferservice.configuration;

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
        //özel yapılandırmalar authentication işlemleri burada yapılması lazım

        http.authorizeHttpRequests(auth -> auth
        // POST /transfers - Müşteri ve Admin
                .requestMatchers(HttpMethod.POST, "/api/v1/transfers").hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // GET tekil transfer sorgulamaları
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/transfers/{transferId}",
                        "/api/v1/transfers/source-iban/{sourceIban}",
                        "/api/v1/transfers/target-iban/{targetIban}",
                        "/api/v1/transfers/source-account/{sourceAccountId}",
                        "/api/v1/transfers/target-account/{targetAccountId}"
                ).hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // GET tüm transferleri duruma göre listeleme (Sadece yetkililer)
                .requestMatchers(HttpMethod.GET, "/api/v1/transfers/status/{status}")
                        .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")


                // PUT transfer iptali (Müşteri sadece kendi transferini iptal edebilir)
                .requestMatchers(HttpMethod.PUT, "/api/v1/transfers/{transferId}/cancel")
                .hasAnyAuthority("CUSTOMER", "ADMIN", "EMPLOYEE")

                // PUT transfer durum güncelleme (Sistem ve yetkililer)
                .requestMatchers(HttpMethod.PUT, "/api/v1/transfers/{transferId}/status").hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/v1/transfers/{transferTransactionId}/transferTransactionId").hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                // Tüm diğer istekler için authentication zorunlu
                .anyRequest().authenticated()
        );

        return http.build();
    }

}
