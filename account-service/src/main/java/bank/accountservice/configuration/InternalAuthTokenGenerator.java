package bank.accountservice.configuration;

import io.github.oguzalpcepni.security.jwt.BaseJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InternalAuthTokenGenerator {
    @Lazy
    private final BaseJwtService baseJwtService;

    public String generateInternalToken() {
        try {
            return baseJwtService.generateToken(
                    "internal",
                    "internal-service",
                    List.of("ADMIN")
            );
        } catch (Exception ex) {
            System.err.println("[Token Error] Failed to generate token: " + ex.getMessage());
            return null; // dikkat: null dönerse interceptor'da if kontrolü koyacağız
        }
    }
}
