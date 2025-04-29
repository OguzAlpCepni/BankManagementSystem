package bank.accountservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "corporate_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateAccount extends Account{

    @Column(nullable = false, unique = true)
    private String taxNumber;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String authorizedPerson;
}
