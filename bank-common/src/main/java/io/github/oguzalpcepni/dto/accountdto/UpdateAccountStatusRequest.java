package io.github.oguzalpcepni.dto.accountdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAccountStatusRequest {
    
    @NotNull(message = "Account ID is required")
    private UUID accountId;
    
    @NotBlank(message = "Status is required")
    private String status;
} 