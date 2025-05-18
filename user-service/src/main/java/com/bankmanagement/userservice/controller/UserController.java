package com.bankmanagement.userservice.controller;

import com.bankmanagement.userservice.entity.Users;
import com.bankmanagement.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Current user: " + authentication.getName() + 
                ", Authorities: " + authentication.getAuthorities());
    }
    
    @GetMapping("/admin-access")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> onlyAdminAccess() {
        return ResponseEntity.ok("You have ADMIN access");
    }
    
    @GetMapping("/customer-access")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<String> onlyCustomerAccess() {
        return ResponseEntity.ok("You have CUSTOMER access");
    }
} 