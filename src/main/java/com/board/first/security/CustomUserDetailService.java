package com.board.first.security;

import com.board.first.entity.User;
import com.board.first.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //username -> email
        return userRepository.findByEmail(username)
                .map(this::createUserDetails) //사용자가 존재하면 User 객체를 UserDetails로 변환
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // DB에 User 값이 존재 한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(User user){
        //사용자의 권한을 나타내는 객체. 예: ROLE_USER, ROLE_ADMIN
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getAuthority().toString());

        //시큐리티 자체적으로 UserDetails 의 구현체인 User
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
