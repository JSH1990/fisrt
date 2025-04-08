package com.board.first.service;

import com.board.first.dto.TokenDto;
import com.board.first.dto.UserRequestDto;
import com.board.first.dto.UserResponseDto;
import com.board.first.entity.RefreshToken;
import com.board.first.entity.User;
import com.board.first.repository.RefreshTokenRepository;
import com.board.first.repository.UserRepository;
import com.board.first.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원 가입
    @Transactional
    public UserResponseDto signup(UserRequestDto userRequestDto){
        if(userRepository.findByEmail(userRequestDto.getEmail()).isPresent()){
            throw new RuntimeException("이미 가입 되어 있는 유저입니다.");
        }

        String authKey = emailService.createKey();
        userRequestDto.setAuthKey(authKey);

        User user = userRequestDto.toUser(passwordEncoder);
        return UserResponseDto.of(userRepository.save(user));
    }


    // 로그인
    @Transactional
    public TokenDto login(UserRequestDto userRequestDto){
        // 1. login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기준으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authenticate);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authenticationToken.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 토큰 발급
        return tokenDto;
    }

}
