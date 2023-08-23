package com.hmdp.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * redis工具类
 *
 * @author ZengXuebin
 * @since 2023/8/23 21:16
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 注入stringRedisTemplate
     *
     * @param stringRedisTemplate redisTemplate
     */
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将对象序列化为json存储再string中 设置TTL过期时间
     *
     * @param key 键
     * @param value 值
     * @param time 过期时间
     * @param unit 单位
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 将对象序列化为json存储再string中 设置TTL过期时间
     *
     * @param key 键
     * @param value 值
     * @param time 过期时间
     * @param unit 单位
     */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value));
    }

    /**
     * 解决缓存穿透
     *
     * @param keyPrefix 缓存前缀
     * @param id redis键
     * @param type redis值类型
     * @param dbFallback sql
     * @param time 过期时间
     * @param unit 时间单位
     * @return 值
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public <K, V> V queryWithPassThrough(String keyPrefix, K id, Class<V> type, Function<K, V> dbFallback,
                                         Long time, TimeUnit unit) {
        // 从redis查询商铺缓存
        String k = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(k);

        // 判断redis是否存在
        // redis存在 直接返回
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }

        // 判断命中的是否为空
        if ("".equals(json)) {
            return null;
        }

        // redis不存在 根据id查询数据库
        V v = dbFallback.apply(id);
        // 数据库不存在 返回error
        if (v == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(k, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 数据库存在 将商铺写入redis 设置30分钟过期剔除
        this.set(k, v, time, unit);

        // 返回
        return v;
    }
}
