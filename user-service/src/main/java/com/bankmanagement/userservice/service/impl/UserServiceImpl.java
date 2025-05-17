package com.bankmanagement.userservice.service.impl;

import com.bankmanagement.userservice.entity.Users;
import com.bankmanagement.userservice.repository.UsersRepository;
import com.bankmanagement.userservice.service.UserService;
import io.github.oguzalpcepni.dto.userdto.UserRegisterRequest;
import io.github.oguzalpcepni.dto.userdto.UserRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserRegisterResponse createUser(UserRegisterRequest userRegisterRequest) {
        if(usersRepository.findByUsername(userRegisterRequest.getUsername()).isPresent()) {
            throw new UsernameNotFoundException("Username " + userRegisterRequest.getUsername() + " already exists");
        }
        if (usersRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()){
            throw new RuntimeException("Email " + userRegisterRequest.getEmail() + " already exists");
        }
        Users user = new Users();
        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));
        user.setEmail(userRegisterRequest.getEmail());
        usersRepository.save(user);


        return new UserRegisterResponse(user.getUsername(),user.getEmail());

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByUsername(username).orElseThrow(() ->new UsernameNotFoundException(username));
    }
}
