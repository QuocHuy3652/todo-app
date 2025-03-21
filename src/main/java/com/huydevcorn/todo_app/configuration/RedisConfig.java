package com.huydevcorn.todo_app.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis.
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    /**
     * Configures and returns a RedisTemplate bean.
     *
     * @return the configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Set key and hash key serializers to String
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Configure the object mapper for date/time serialization
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Set value and hash value serializers to JSON
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
