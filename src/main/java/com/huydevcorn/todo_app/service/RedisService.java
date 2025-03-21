package com.huydevcorn.todo_app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with Redis.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    /**
     * Sets an object in Redis with a specified timeout.
     *
     * @param key the key under which the object is stored
     * @param data the object to store
     * @param timeout the timeout duration
     * @param unit the time unit of the timeout
     */
    public void setObject(String key, Object data, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, data, timeout, unit);
    }

    /**
     * Retrieves an object from Redis.
     *
     * @param key the key of the object to retrieve
     * @param typeReference the type reference of the object
     * @param <T> the type of the object
     * @return the retrieved object, or null if not found
     */
    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, typeReference);
    }

    /**
     * Deletes keys matching a pattern from Redis.
     *
     * @param pattern the pattern to match keys
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key the key to delete
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
