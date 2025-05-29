package bank.customerservice.rule;

import bank.customerservice.entity.Customers;
import bank.customerservice.repository.CustomersRepository;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerBusinessRule {


    private final CustomersRepository customersRepository;

    public CustomerBusinessRule(CustomersRepository customersRepository) {
        this.customersRepository = customersRepository;
    }


    public void checkEmailExists(String email) {
        if(customersRepository.findByEmail(email).isPresent()) throw new BusinessException("Email already exists");
    }

    public void checkPhoneExists(String phone) {
        Optional<Customers> customer = customersRepository.findByPhone(phone);
        if(customer.isPresent()) throw new BusinessException("Phone already exists");
    }


}
