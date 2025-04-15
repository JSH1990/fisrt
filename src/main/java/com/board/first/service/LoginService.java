package com.board.first.service;

import com.board.first.dto.TokenDto;
import com.board.first.dto.TokenRequestDto;
import com.board.first.dto.UserRequestDto;
import com.board.first.dto.UserResponseDto;
import com.board.first.entity.RefreshToken;
import com.board.first.entity.User;
import com.board.first.repository.RefreshTokenRepository;
import com.board.first.repository.UserRepository;
import com.board.first.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.IllegalFormatCodePointException;
import java.util.Optional;

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

    // 토근 재발급
    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if(!tokenProvider.validateToken(tokenRequestDto.getAccessToken())){
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())){
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(refreshToken);

        // 토큰 발급
        return tokenDto;
    }

    // 이메일 유효한지 확인
    public boolean isValidEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }

    // 토큰 유효하면 User 정보 가져오기
    public User validateTokenAndGetUser(HttpServletRequest request, UserDetails userDetails){
        String accessToken = request.getHeader("Authorization");
        if(accessToken != null && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        // 토큰 유효한지 검증
        if(accessToken != null && tokenProvider.validateToken(accessToken)){
            Long Id = Long.valueOf(userDetails.getUsername());
            User user = userRepository.findById(Id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다."));
            return user;
        }else {
            throw new IllegalStateException("토큰이 만료 되었습니다. Refresh Token을 보내주세요");
        }
    }

    // 비밀번호 변경
    public boolean changePwd(User user, HttpServletRequest request, UserDetails userDetails){
        User authUser = validateTokenAndGetUser(request, userDetails);
        User member = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));
        String encodePassword = passwordEncoder.encode(user.getPassword());
        member.setPassword(encodePassword);
        User saveMember = userRepository.save(member);
        log.info(saveMember.toString());

        return true;
    }
}
