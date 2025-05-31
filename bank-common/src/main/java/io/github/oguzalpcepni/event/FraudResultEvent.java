package io.github.oguzalpcepni.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudResultEvent {
    private UUID loanId;
    private boolean fraudCheckPassed;
}
