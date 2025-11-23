package com.example.orderpay.auth;

import com.example.orderpay.member.Role;
import com.example.orderpay.member.User;
import com.example.orderpay.member.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate; // ✅ Redis 사용

    // 인증 코드 유효시간 (1시간)
    private static final long CODE_EXPIRATION_MINUTES = 60;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil,
                       TokenBlacklistService blacklistService, PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender, StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
    }

    // ✅ Gmail만 허용
    private void validateGmail(String email) {
        if (email == null || !email.toLowerCase().endsWith("@gmail.com")) {
            throw new RuntimeException("Gmail 주소만 사용할 수 있습니다.");
        }
    }

    // ✅ 이메일 인증 코드 발송
    public void sendEmailVerificationCode(String email) {
        validateGmail(email);

        String code = generateVerificationCode();
        String key = "email:verify:" + email;

        // Redis에 코드 저장 (1시간 유효)
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        sendEmail(email, "이메일 인증 코드", "회원가입 인증 코드: " + code + "\n유효시간: 1시간");
    }

    // ✅ 이메일 코드 검증
    public boolean verifyEmailCode(String email, String code) {
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            throw new RuntimeException("인증 코드가 만료되었거나 존재하지 않습니다.");
        }

        if (!storedCode.equals(code)) {
            throw new RuntimeException("인증 코드가 올바르지 않습니다.");
        }

        // 인증 완료 시 Redis에서 삭제
        redisTemplate.delete(key);
        return true;
    }

    // ✅ 랜덤 6자리 숫자 코드 생성
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // ✅ 회원가입
    public String signup(String username, String password, String email,String roleStr) {
        System.out.println("===== 회원가입 시작 =====");
        System.out.println("입력 username: " + username);
        System.out.println("입력 email: " + email);
        System.out.println("입력 password: " + password);
        System.out.println("입력 roleStr: " + roleStr);
        validateGmail(email);

        // Optional 체크
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        validatePassword(password);

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("역할은 USER 또는 OWNER만 가능합니다.");
        }

        User user = new User(username, passwordEncoder.encode(password), email, role);
        userRepository.save(user);

        sendEmail(email, "회원가입 완료", username + "님, 회원가입이 완료되었습니다.");

        return "회원가입 성공. 이메일을 확인하세요.";
    }

    // ✅ 비밀번호 규칙
    public void validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password.length() < 8) errors.add("비밀번호는 8자 이상이어야 합니다.");
        if (!password.matches(".*[A-Z].*")) errors.add("대문자를 포함해야 합니다.");
        if (!password.matches(".*[a-z].*")) errors.add("소문자를 포함해야 합니다.");
        if (!password.matches(".*[0-9].*")) errors.add("숫자를 포함해야 합니다.");
        if (!password.matches(".*[!@#$%^&*()].*")) errors.add("특수문자를 포함해야 합니다.");

        if (!errors.isEmpty()) throw new RuntimeException(String.join(" ", errors));
    }

    // ✅ 아이디 찾기
    public String findUsernameByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록된 이메일이 없습니다."));

        sendEmail(user.getEmail(), "아이디 찾기", "회원님의 아이디: " + user.getUsername());
        return "아이디를 이메일로 발송했습니다.";
    }

    // ✅ 비밀번호 재설정
    public String resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록된 이메일이 없습니다."));


        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        sendEmail(user.getEmail(), "비밀번호 재설정", "비밀번호가 성공적으로 변경되었습니다.");
        return "비밀번호가 재설정되었습니다. 이메일을 확인하세요.";
    }

    // ✅ 이메일 전송
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // ✅ 로그인
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("로그인 실패"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("로그인 실패");
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    // ✅ 로그아웃
    public String logout(String token) {
        String username = jwtUtil.getUsername(token);
        if (username != null) {
            long expire = jwtUtil.getExpirationMillis(token);
            blacklistService.blacklistToken(token, expire);
        }
        return "로그아웃 성공";
    }

    // ✅ 로그인한 사용자 계정 탈퇴
    public String deleteUser(User loginUser, String password) {
        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        if (!passwordEncoder.matches(password, loginUser.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        userRepository.delete(loginUser);

        sendEmail(loginUser.getEmail(), "회원 탈퇴 완료", loginUser.getUsername() + "님, 회원 탈퇴가 완료되었습니다.");

        return "회원 탈퇴가 완료되었습니다.";
    }


}