package bank.transactionservice.client;

import io.github.oguzalpcepni.dto.accountdto.CreditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountClient {
    @PostMapping("/api/v1/accounts/{iban}/credit")
    ResponseEntity<Boolean> creditAccount(
            @PathVariable String iban,
            @RequestBody CreditRequest creditRequest);
            
    // Telafi edici işlem için compensate metodu
    @PostMapping("/api/v1/accounts/{iban}/compensate")
    ResponseEntity<Boolean> compensateDebit(
            @PathVariable String iban,
            @RequestBody CreditRequest creditRequest);
}
