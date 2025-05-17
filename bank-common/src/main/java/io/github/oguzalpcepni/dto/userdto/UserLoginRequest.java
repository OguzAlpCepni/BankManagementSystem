package io.github.oguzalpcepni.dto.userdto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginRequest {

    private String username;
    private String password;

}
