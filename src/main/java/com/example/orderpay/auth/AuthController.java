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

    @PostMapping("/send-code") // 비번 바꾸기 이메일 전송
    public ResponseEntity<String> sendEmailCode(@RequestParam("email") String email) {
        try {
            authService.sendEmailVerificationCode(email); // 리턴값 없음
            return ResponseEntity.ok("이메일 인증 번호 발송");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 회원가입 (이메일 인증 포함) // 수정됨
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        try {
            authService.signup(user.getUsername(), user.getPassword(), user.getEmail());
            return ResponseEntity.ok("회원가입 성공 (이메일 인증 완료)"); // 수정됨
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 아이디 찾기용 이메일 발송
    @PostMapping("/send-code-find")
    public ResponseEntity<String> sendFindEmailCode(@RequestParam("email") String email) {
        try {
            String msg = authService.findUsernameByEmail(email); // 기존 로직 그대로
            return ResponseEntity.ok(msg);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 비밀번호 재설정 // 추가됨
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("email") String email,
                                                @RequestParam("newPassword") String newPassword) {
        try {
            String result = authService.resetPassword(email, newPassword);
            return ResponseEntity.ok(result); // 200 OK 반환
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 인증 코드 검증 추가됨
    @PostMapping("/verify-email") // 추가됨
    public ResponseEntity<String> verifyEmail(@RequestParam("email") String email,
                                              @RequestParam("code") String code) {
        try {
            boolean verified = authService.verifyEmailCode(email, code); // 이메일 코드 확인
            if (verified) {
                return ResponseEntity.ok("이메일 인증 성공");
            } else {
                return ResponseEntity.badRequest().body("이메일 인증 실패");
            }
        } catch (RuntimeException e) {
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

