package com.bankmanagement.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "operation_claims")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OperationClaim {
    @Id
    @UuidGenerator
    private UUID id;
    
    private String name;
    
    @ManyToMany(mappedBy = "operationClaims")
    private Set<Users> users = new HashSet<>();
} 