package com.bankmanagement.userservice.service;

import com.bankmanagement.userservice.entity.Users;
import io.github.oguzalpcepni.dto.userdto.UserLoginRequest;
import io.github.oguzalpcepni.dto.userdto.UserRegisterRequest;
import io.github.oguzalpcepni.dto.userdto.UserRegisterResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserRegisterResponse createUser(UserRegisterRequest userRegisterRequest);
    String login(UserLoginRequest userLoginRequest);
}
