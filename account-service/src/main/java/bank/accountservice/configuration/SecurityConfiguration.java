package bank.accountservice.configuration;

import io.github.oguzalpcepni.security.configuration.BaseSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final BaseSecurityService baseSecurityService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Önce ortak güvenlik ayarlarını uygula
        http = baseSecurityService.configureCoreSecurity(http);
        
        // Account-service'e özel yetkilendirme kuralları
        http.authorizeHttpRequests(auth -> auth
                // Hesap doğrulama ve işlem yapma endpointleri - sadece servisler arası iletişim için
                .requestMatchers(
                    "/api/v1/accounts/*/validate", 
                    "/api/v1/accounts/iban/*/validate",
                    "/api/v1/accounts/*/balance-check",
                    "/api/v1/accounts/*/debit",
                    "/api/v1/accounts/*/credit",
                    "/api/v1/accounts/*/compensate"
                ).hasAuthority("ADMIN")
                
                // Temel hesap bilgileri görüntüleme - müşteriler erişebilir
                .requestMatchers(
                    "/api/v1/accounts/*",
                    "/api/v1/accounts/iban/*"
                ).hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")
                
                // Bireysel hesap işlemleri
                .requestMatchers("/api/v1/individual-accounts/*/withdraw", 
                                "/api/v1/individual-accounts/*/deposit")
                .hasAuthority("CUSTOMER")
                
                // Kurumsal hesap işlemleri
                .requestMatchers("/api/v1/corporate-accounts/*/withdraw", 
                                "/api/v1/corporate-accounts/*/deposit")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE")
                
                // Hesap listeleme işlemleri - yönetici ve çalışanlar
                .requestMatchers("/api/v1/individual-accounts",
                                "/api/v1/corporate-accounts")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")
                
                // Hesap durumu güncelleme - sadece yönetici ve çalışanlar
                .requestMatchers("/api/v1/individual-accounts/*/status",
                                "/api/v1/corporate-accounts/*/status")
                .hasAnyAuthority("ADMIN", "EMPLOYEE")
                
                // Hesap oluşturma - herkes yapabilir (müşteri kayıt sonrası)
                .requestMatchers("/api/v1/individual-accounts", 
                                "/api/v1/corporate-accounts")
                .permitAll()
                
                // Diğer tüm endpointler için authentication gerekli 
                .anyRequest().authenticated()
        );
        
        return http.build();
    }
}
