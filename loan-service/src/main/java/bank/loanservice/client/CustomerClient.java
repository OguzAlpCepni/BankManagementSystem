package bank.loanservice.client;

import io.github.oguzalpcepni.dto.CustomerDto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}")
    ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable("id") String id);
}
