package com.example.orderpay.auth;

import com.example.orderpay.member.User;
import com.example.orderpay.member.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil,
                          TokenBlacklistService blacklistService, PasswordEncoder passwordEncoder,AuthService authService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        try {
            // AuthService.signup()에서 아이디 중복 체크 + 비밀번호 정책 체크 + 암호화 후 저장
            authService.signup(user.getUsername(), user.getPassword());
            return ResponseEntity.ok("회원가입 성공");
        } catch (RuntimeException e) {
            // 발생한 예외 메시지를 그대로 프론트로 전달
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            String token = authService.login(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("토큰이 없거나 잘못되었습니다.");
        }
        String token = authHeader.substring(7); // "Bearer " 제거
        return ResponseEntity.ok(authService.logout(token));
    }


}

