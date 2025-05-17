package io.github.oguzalpcepni.security.configuration;

import io.github.oguzalpcepni.security.filter.BaseJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BaseSecurityService {
    private BaseJwtAuthFilter baseJwtAuthFilter;
    private static final String[] WHITE_LIST_URLS = {
            "/swagger-ui/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/api/v1/auth/**"
    };
    public HttpSecurity configureCoreSecurity(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req-> req.requestMatchers(WHITE_LIST_URLS).permitAll())
                .addFilterBefore(baseJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity;
    }
// feign clientlerin securit ypılandırlılması araştır
    //identity service login işlemii diğer tüm servislerin jwt işlemlerine bakman lazım
    // bütün servislerdeki paket şeyini güncelle
}
