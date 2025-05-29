package bank.customerservice.controller;

import bank.customerservice.service.CustomerService;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerRequestDto;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomersController {
    private final CustomerService customerService;


    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getCustomers());
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomer());
    }


    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@RequestBody @Valid  CustomerRequestDto requestDto) {
        return new ResponseEntity<>(customerService.createCustomer(requestDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable String id,
            @RequestBody @Valid CustomerRequestDto requestDto
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(requestDto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer();
        return ResponseEntity.noContent().build();
    }


}
