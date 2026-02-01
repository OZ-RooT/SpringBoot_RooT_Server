package io.github._3xhaust.root_server.infrastructure.redis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void set(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.error("Failed to set cache for key: {}", key, e);
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            T value = objectMapper.readValue(json, clazz);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            T value = objectMapper.readValue(json, typeReference);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete cache for key: {}", key, e);
        }
    }

    public void deletePattern(String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.error("Failed to delete cache pattern: {}", pattern, e);
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check cache existence for key: {}", key, e);
            return false;
        }
    }
}
