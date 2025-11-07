package com.example.orderpay.auth;

import com.example.orderpay.member.User;
import com.example.orderpay.member.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil,
                       TokenBlacklistService blacklistService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public String signup(String username, String password) {
        // 아이디 중복 체크
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        // 비밀번호 정책 체크
        validatePassword(password);

        // 암호화 후 저장
        User user = new User(username, passwordEncoder.encode(password));
        userRepository.save(user);
        return "회원가입 성공";
    }

    // 비밀번호 정책 검사
    public void validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if(password.length() < 8) errors.add("비밀번호는 8자 이상이어야 합니다.");
        if(!password.matches(".*[A-Z].*")) errors.add("대문자를 포함해야 합니다.");
        if(!password.matches(".*[a-z].*")) errors.add("소문자를 포함해야 합니다.");
        if(!password.matches(".*[0-9].*")) errors.add("숫자를 포함해야 합니다.");
        if(!password.matches(".*[!@#$%^&*()].*")) errors.add("특수문자를 포함해야 합니다.");

        if(!errors.isEmpty()) {
            throw new RuntimeException(String.join(" ", errors));
        }
    }



    // 로그인
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("로그인 실패");
        }
        return jwtUtil.generateToken(user.getUsername());
    }

    // 로그아웃
    public String logout(String token) {
        String username = jwtUtil.getUsername(token);
        if (username != null) {
            long expire = jwtUtil.getExpirationMillis(token);
            blacklistService.blacklistToken(token, expire);
        }
        return "로그아웃 성공";
    }

}
