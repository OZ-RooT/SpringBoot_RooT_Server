package io.github._3xhaust.root_server.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(Long userId, String rawToken, Duration expiration) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String hashed = hash(rawToken);
        redisTemplate.opsForValue().set(key, hashed, expiration);
    }

    public Optional<String> getStoredHashedToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token != null ? token.toString() : null);
    }

    public boolean matches(Long userId, String rawToken) {
        return getStoredHashedToken(userId)
                .map(stored -> stored.equals(hash(rawToken)))
                .orElse(false);
    }

    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public boolean existsRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.hasKey(key);
    }

    @Deprecated
    public Optional<Long> getUserIdByToken(String rawToken) {
        String pattern = REFRESH_TOKEN_PREFIX + "*";
        var keys = redisTemplate.keys(pattern);
        String hashed = hash(rawToken);
        for (String key : keys) {
            Object storedToken = redisTemplate.opsForValue().get(key);
            if (hashed.equals(storedToken)) {
                String userIdStr = key.replace(REFRESH_TOKEN_PREFIX, "");
                return Optional.of(Long.parseLong(userIdStr));
            }
        }
        return Optional.empty();
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
