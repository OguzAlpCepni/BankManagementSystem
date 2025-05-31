package io.github.oguzalpcepni.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanUnderwritingCompletedEvent {

    UUID loanId;
    int creditScore;
    boolean fraudCheckPassed;
}
