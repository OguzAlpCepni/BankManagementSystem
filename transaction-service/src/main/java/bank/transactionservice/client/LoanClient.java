package bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient("loan-service")
public interface LoanClient {

    @GetMapping("/api/v1/loan-application/credit-scre/{customerId}")
    ResponseEntity<Integer> getCreditScore(@PathVariable("customerId") UUID customerId);

}
