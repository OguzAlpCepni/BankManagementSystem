package io.github.oguzalpcepni.dto.accountdto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountSearchRequest {
    private Long customerId;
    private String iban;
    private String identityNumber;
    private String taxNumber;
    private String type;
    private String status;
    private String currency;
} 