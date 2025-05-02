package io.github.oguzalpcepni.dto.accountdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndividualAccountDto extends AccountDto {
    private String identityNumber;
    private String firstName;
    private String lastName;
} 