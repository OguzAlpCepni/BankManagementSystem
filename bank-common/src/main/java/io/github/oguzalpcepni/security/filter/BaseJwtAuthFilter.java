package io.github.oguzalpcepni.security.filter;

import io.github.oguzalpcepni.security.jwt.BaseJwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;

@Component
public class BaseJwtAuthFilter extends OncePerRequestFilter {

    private final BaseJwtService baseJwtService;

    public BaseJwtAuthFilter(BaseJwtService baseJwtService) {
        this.baseJwtService = baseJwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwtHeader = request.getHeader("Authorization");

        if (jwtHeader != null && jwtHeader.startsWith("Bearer ")) {
            String jwt = jwtHeader.substring(7);
            String username = baseJwtService.extractUsername(jwt);
            System.out.println("username: " + username);
            List<String> roles = baseJwtService.extractRoles(jwt);
            if(roles == null ||roles.size() <= 0)
            {
                throw new RuntimeException("Rols not found");
            }
            List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
            if (baseJwtService.verifyToken(jwt)){
                UsernamePasswordAuthenticationToken token = new
                        UsernamePasswordAuthenticationToken(username, null, authorities);
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(token);
        }
        }
        filterChain.doFilter(request,response);
    }
}
