package io.github.oguzalpcepni.dto.CustomerDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerResponseDto {
    String id;
    String firstName;
    String lastName;
    String email;
    String phone;
    String address;
    String city;
}
