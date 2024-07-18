package com.example.chatbottads.config.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RedisLogger {

    private static final Logger logger = LoggerFactory.getLogger(RedisLogger.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final RedisTemplate<String, String> redisTemplate;

    public RedisLogger(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("%s [%s]: %s", timestamp, level, message);

        // Enviar log para o Redis
        redisTemplate.opsForList().rightPush("logs", logMessage);

        // Tambem exibir log no console
        switch (level.toLowerCase()) {
            case "info" -> logger.info(logMessage);
            case "warn" -> logger.warn(logMessage);
            case "error" -> logger.error(logMessage);
            default -> logger.debug(logMessage);
        }
    }
}
