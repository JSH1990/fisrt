package com.board.first.entity;

import com.board.first.Authority.Authority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String authKey;
    private String role;
    private String address;
    private LocalDateTime createdDate;


    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Builder
    public User(String email, String password, Authority authority,  String authKey){
        this.email = email;
        this.password = password;
        this.authKey = authKey;
        this.authority = authority;

    }
}
