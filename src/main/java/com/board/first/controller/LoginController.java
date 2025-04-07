package com.board.first.controller;

import com.board.first.dto.UserRequestDto;
import com.board.first.dto.UserResponseDto;
import com.board.first.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/First")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.ok(loginService.signUp(userRequestDto));
    }
}
