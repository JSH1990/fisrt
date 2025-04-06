package com.board.first.service;

import com.board.first.dto.LoginUserDto;
import com.board.first.dto.UserDto;
import com.board.first.dto.UserRequestDto;
import com.board.first.entity.User;
import com.board.first.repository.UserRepository;
import com.board.first.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider TokenProvider;
    private final EmailService emailService;

    // 회원 가입
    @Transactional
    public UserResponseDto signUp(UserRequestDto userRequestDto){
        if(userRepository.findByEmail(userRequestDto.getEmail()).isPresent()){
            throw new RuntimeException("이미 가입 되어 있는 유저입니다.");
        }

        String authKey = emailService.createKey();
        userRequestDto.setAuthKey(authKey);

        User user = userRequestDto.toUser(passwordEncoder);
        return UserResponseDto.of(userRepository.save(user));
    }


}
