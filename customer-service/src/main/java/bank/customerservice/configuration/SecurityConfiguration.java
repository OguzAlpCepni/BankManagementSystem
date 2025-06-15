package bank.customerservice.configuration;

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

        // Customer-service'e özel yetkilendirme kuralları
        http.authorizeHttpRequests(auth -> auth
                // Tüm müşterileri listeleme - sadece yönetici ve çalışanlar
                .requestMatchers(HttpMethod.GET, "/api/v1/customers")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // Müşteri detaylarını görüntüleme - müşteri kendi bilgilerini, çalışan/yönetici tüm bilgileri görebilir
                .requestMatchers(HttpMethod.GET, "/api/v1/customers/**")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // Yeni müşteri oluşturma - herkese açık (kayıt işlemi için)
                .requestMatchers(HttpMethod.POST, "/api/v1/customers")
                .permitAll()

                // Müşteri bilgilerini güncelleme - müşteri kendi bilgilerini, çalışan/yönetici tüm bilgileri güncelleyebilir
                .requestMatchers(HttpMethod.PUT, "/api/v1/customers/**")
                .hasAnyAuthority("CUSTOMER", "EMPLOYEE", "ADMIN")

                // Müşteri silme - sadece yöneticiler
                .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**")
                .hasAuthority("ADMIN")

                // Diğer tüm endpointler için authentication gerekli
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
