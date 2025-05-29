package bank.customerservice.service.impl;

import bank.customerservice.entity.Customers;
import bank.customerservice.repository.CustomersRepository;
import bank.customerservice.rule.CustomerBusinessRule;
import bank.customerservice.service.CustomerService;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerRequestDto;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerResponseDto;
import io.github.oguzalpcepni.exceptions.type.BusinessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomersRepository customersRepository;
    private final CustomerBusinessRule customerBusinessRule;

    public CustomerServiceImpl(CustomersRepository customersRepository, CustomerBusinessRule customerBusinessRule) {
        this.customersRepository = customersRepository;
        this.customerBusinessRule = customerBusinessRule;
    }


    @Override
    public List<CustomerResponseDto> getCustomers() {
        List<Customers> customers = customersRepository.findAll();
        return  customers.stream().map(this::getCustomerResponseDto).collect(Collectors.toList());

    }

    @Override
    public CustomerResponseDto getCustomer() {
        Customers customer = customersRepository.findById(getUserId()).orElseThrow(() -> new BusinessException("could not found customer by id"));
        return getCustomerResponseDto(customer);
    }

    @Override
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto) {
        customerBusinessRule.checkEmailExists(customerRequestDto.getEmail());
        customerBusinessRule.checkPhoneExists(customerRequestDto.getPhone());
        Customers customers = getCustomerMap(customerRequestDto);
        customers.setId(getUserId()); // burada userId ile customerId nin aynı olmasını sağladım.

        customersRepository.save(customers);

        return getCustomerResponseDto(customers);
    }

    @Override
    public CustomerResponseDto updateCustomer( CustomerRequestDto customerRequestDto) {
        Customers customers = customersRepository.findById(getUserId()).orElseThrow(() -> new BusinessException("could not found customer by id"));
        customers.setId(customers.getId());
        customers.setFirstName(customerRequestDto.getFirstName());
        customers.setLastName(customerRequestDto.getLastName());
        customers.setEmail(customerRequestDto.getEmail());
        customers.setPhone(customerRequestDto.getPhone());
        customers.setAddress(customerRequestDto.getAddress());
        customers.setCity(customerRequestDto.getCity());
        customersRepository.save(customers);
        return getCustomerResponseDto(customers);
    }

    @Override
    public void deleteCustomer() {
        customersRepository.deleteById(getUserId());
    }

    private CustomerResponseDto getCustomerResponseDto(Customers customers) {
        CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        customerResponseDto.setId(customers.getId());
        customerResponseDto.setFirstName(customers.getFirstName());
        customerResponseDto.setLastName(customers.getLastName());
        customerResponseDto.setEmail(customers.getEmail());
        customerResponseDto.setPhone(customers.getPhone());
        customerResponseDto.setAddress(customers.getAddress());
        customerResponseDto.setCity(customers.getCity());
        return customerResponseDto;
    }
    private Customers getCustomerMap(CustomerRequestDto customerRequestDto) {
        Customers customers = new Customers();
        customers.setFirstName(customerRequestDto.getFirstName());
        customers.setLastName(customerRequestDto.getLastName());
        customers.setEmail(customerRequestDto.getEmail());
        customers.setPhone(customerRequestDto.getPhone());
        customers.setAddress(customerRequestDto.getAddress());
        customers.setCity(customerRequestDto.getCity());
        return customers;
    }
    private String getUserId(){
        return SecurityContextHolder.getContext().getAuthentication().getName(); // sub alanını user id yaptım
    }

}
