package com.bankmanagement.userservice.controller;

import com.bankmanagement.userservice.entity.Users;
import com.bankmanagement.userservice.service.UserService;
import io.github.oguzalpcepni.dto.userdto.UserRegisterRequest;
import io.github.oguzalpcepni.dto.userdto.UserRegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(userService.createUser(userRegisterRequest));
    }

}

