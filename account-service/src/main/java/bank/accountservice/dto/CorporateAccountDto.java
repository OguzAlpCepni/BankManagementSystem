package bank.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateAccountDto extends AccountDto {
    private String taxNumber;
    private String companyName;
    private String authorizedPerson;
} 