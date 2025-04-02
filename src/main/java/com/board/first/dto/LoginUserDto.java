package com.board.first.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@NoArgsConstructor
public class LoginUserDto {

    @NotEmpty(message = "이메일은 필수 입니다.")
    private String email;

    @NotEmpty(message = "비밀번호는 필수 입니다.")
    private String password;
}
