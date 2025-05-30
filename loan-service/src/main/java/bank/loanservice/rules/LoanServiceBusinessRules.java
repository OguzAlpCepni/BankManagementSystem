package bank.loanservice.rules;

import bank.loanservice.client.CustomerClient;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoanServiceBusinessRules {

    private final CustomerClient customerClient;

    public void checkExistsCustomer(String id){
        ResponseEntity<CustomerResponseDto> customerResponseDto = customerClient.getCustomerById(id);


    }

}
