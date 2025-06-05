package io.github.oguzalpcepni.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BillInquiryDto {

    private String billerCode;
    private String subscriberNumber;
    private String billType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime dueDate;
    private String institutionName;
    private boolean payable;

}
