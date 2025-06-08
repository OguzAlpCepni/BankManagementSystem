package bank.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("payment-service")
public interface PaymentClient {

    @PostMapping("/api/v1/payment/pay")
    ResponseEntity<Map<String,Object>> pay(
            @RequestBody Map<String,Object> req
    );


}
