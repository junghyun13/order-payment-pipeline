package com.example.orderpay.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expireMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(expireMillis));
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
