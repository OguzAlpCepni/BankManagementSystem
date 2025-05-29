package bank.customerservice.service;

import io.github.oguzalpcepni.dto.CustomerDto.CustomerRequestDto;
import io.github.oguzalpcepni.dto.CustomerDto.CustomerResponseDto;

import java.util.List;

public interface CustomerService {

    List<CustomerResponseDto> getCustomers(); // admin erişsin
    CustomerResponseDto getCustomer(); // bir dene secu
    CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto);
    CustomerResponseDto updateCustomer(CustomerRequestDto customerRequestDto);
    void deleteCustomer();


}
