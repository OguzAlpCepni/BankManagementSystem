package io.github.oguzalpcepni.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class QrPaymentDto {
    @NotBlank(message = "QR kod boş olamaz")
    private String qrCode;

    @NotNull(message = "Kullanıcı ID boş olamaz")
    private UUID userId;

    @NotNull(message = "Hesap ID boş olamaz")
    private UUID accountId;
}
