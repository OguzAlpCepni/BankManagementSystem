package bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountServiceClient {
    
    @GetMapping("/api/v1/accounts/{iban}/validate")
    ResponseEntity<Boolean> validateAccount(@PathVariable("iban") String iban);
    
    @GetMapping("/api/v1/accounts/{iban}/balance")
    ResponseEntity<BigDecimal> getAccountBalance(@PathVariable("iban") String iban);
    
    @PostMapping("/api/v1/accounts/{iban}/debit")
    ResponseEntity<Boolean> debitAccount(@PathVariable("iban") String iban, @RequestBody DebitRequest request);
    
    @PostMapping("/api/v1/accounts/{iban}/credit")
    ResponseEntity<Boolean> creditAccount(@PathVariable("iban") String iban, @RequestBody CreditRequest request);
    
    record DebitRequest(BigDecimal amount, String currency, UUID transactionId, String description) {}
    
    record CreditRequest(BigDecimal amount, String currency, UUID transactionId, String description) {}
}
 