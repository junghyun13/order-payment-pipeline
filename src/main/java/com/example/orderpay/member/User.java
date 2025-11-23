package com.example.orderpay.member;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "users") // PostgreSQL 예약어 방지
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email; // 이메일 추가

    // 역할 추가 (USER, OWNER)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 커스텀 생성자 (필요 시)
    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

}