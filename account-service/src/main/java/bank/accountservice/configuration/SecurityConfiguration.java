package bank.accountservice.configuration;

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
        // Önce ortak güvenlik ayarlarını uygula
        http = baseSecurityService.configureCoreSecurity(http);

        http.authorizeHttpRequests(auth -> auth
                // ----------------------
                // Temel Hesap İşlemleri
                // ----------------------
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/{id}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/iban/{iban}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // Servisler arası iletişim için internal endpoint'ler
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/{id}/validate")
                .hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/iban/{iban}/validate")
                .hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/{id}/balance-check")
                .hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts/{id}/debit")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts/{iban}/credit")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/accounts/{iban}/compensate")
                .hasAuthority("ADMIN")

                // ----------------------
                // Kurumsal Hesaplar
                // ----------------------
                .requestMatchers(HttpMethod.POST, "/api/v1/corporate-accounts")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/corporate-accounts")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/api/v1/corporate-accounts/{id}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/corporate-accounts/customer/{customerId}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/corporate-accounts/iban/{iban}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/corporate-accounts/tax-number/{taxNumber}")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/corporate-accounts/{id}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/corporate-accounts/{id}/deposit")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/corporate-accounts/{id}/withdraw")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/corporate-accounts/{id}/status")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")

                // ----------------------
                // Bireysel Hesaplar
                // ----------------------
                .requestMatchers(HttpMethod.POST, "/api/v1/individual-accounts")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/individual-accounts")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/api/v1/individual-accounts/{id}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/individual-accounts/customer/{customerId}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/individual-accounts/iban/{iban}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/individual-accounts/{id}")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/individual-accounts/{id}/deposit")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/individual-accounts/{id}/withdraw")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/individual-accounts/{id}/status")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")

                // ----------------------
                // Varsayılan Kurallar
                // ----------------------
                .anyRequest().authenticated()
        );
        
        return http.build();
    }
}
