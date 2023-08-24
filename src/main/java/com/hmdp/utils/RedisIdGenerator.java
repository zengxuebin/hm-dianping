package com.hmdp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * redisID生成器
 *
 * @author ZengXuebin
 * @since 2023/8/24 20:15
 */
@Component
public class RedisIdGenerator {

    /**
     * 2022年1月1日0点时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;

    /**
     * 序列号位数
     */
    private static final int SERIAL_BIT = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成redisId
     * @param keyPrefix 业务前缀
     * @return id
     */
    public long generateNextId(String keyPrefix) {
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 生成序列号
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key = "inc:" + keyPrefix + ":" + nowDate;
        // 自增长
        long count = stringRedisTemplate.opsForValue().increment(key);

        // 拼接并返回
        return timestamp << SERIAL_BIT | count;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        long second = localDateTime.toEpochSecond(ZoneOffset.UTC);
        System.out.println(second);
    }
}
