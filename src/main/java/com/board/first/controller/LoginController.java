package com.board.first.controller;

import com.board.first.dto.TokenDto;
import com.board.first.dto.TokenRequestDto;
import com.board.first.dto.UserRequestDto;
import com.board.first.dto.UserResponseDto;
import com.board.first.entity.User;
import com.board.first.service.EmailService;
import com.board.first.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/First")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService; //test
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.ok(loginService.signup(userRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto userRequestDto){
        return ResponseEntity.ok(loginService.login(userRequestDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenRequestDto tokenRequestDto){
        return ResponseEntity.ok(loginService.reissue(tokenRequestDto));
    }

    /**
     * 이메일이 데이터베이스에 존재하는지 확인
     */
    @GetMapping("/{email}")
    public ResponseEntity<Boolean> findMemberByEmail(@PathVariable String email) {
        boolean isValid = loginService.isValidEmail(email);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 이메일 중복여부 체크, 중복안되면 새로 생성해서 이메일 전송
     */
    @PostMapping("/main/email")
    @ResponseBody
    public Object findEmailOverlap(@RequestBody Map<String, String> findEmailOver) throws Exception {
        String email = findEmailOver.get("emailOverlap");
        boolean isOverlap = emailService.emailOverlap(email);
        if(isOverlap){
            return false;
        }else{
            String code = emailService.sendSimpleMessage(email);
            log.info("인증 코드: " + code);
            return code;
        }
    }

    @PutMapping("/main/changePwd")
    public ResponseEntity<?> changePwd(@RequestBody User user, HttpServletRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails){
        try {
            boolean isUpdate = loginService.changePwd(user, request, userDetails);
            return ResponseEntity.ok("비밀번호 변경");
        }catch (IllegalAccessError e){
            return ResponseEntity.badRequest().body("비밀번호 변경 실패" + e.getMessage());
        }
    }
}
