package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 商铺服务接口实现层
 *
 * @author ZengXuebin
 * @since 2023/8/22 00:41
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    public StringRedisTemplate stringRedisTemplate;

    /**
     * 查询商铺
     *
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result queryShopById(Long id) {
        // 解决缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }

        // 返回
        return Result.ok(shop);
    }

    /**
     * 互斥锁解决缓存击穿
     * @param id 商铺id
     * @return 商铺信息
     */
    public Shop queryWithMutex(Long id) {
        // 从redis查询商铺缓存
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 判断redis是否存在
        // redis存在 直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中的是否为空
        if ("".equals(shopJson)) {
            return null;
        }

        // 未命中 实现缓存重建
        // 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 判断是否获取成功
            // 获取失败 休眠并重试
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            // 获取成功 先检测redis缓存是否存在 做到双重校验
            // 从redis查询商铺缓存
            String shopCache = stringRedisTemplate.opsForValue().get(shopKey);

            // 判断redis是否存在
            // redis存在 直接返回
            if (StrUtil.isNotBlank(shopJson)) {
                return JSONUtil.toBean(shopCache, Shop.class);
            }
            // redis不存在 再根据id查询数据库
            shop = this.getById(id);
            Thread.sleep(200);
            // 数据库不存在 返回error
            if (shop == null) {
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 将商铺写入redis 设置30分钟过期剔除
            stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop),
                    RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放互斥锁
            unLock(lockKey);
        }
        return shop;
    }

    /**
     * 解决缓存穿透
     * @param id 商铺id
     * @return 商铺信息
     */
    public Shop queryWithPassThrough(Long id) {
        // 从redis查询商铺缓存
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);

        // 判断redis是否存在
        // redis存在 直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中的是否为空
        if ("".equals(shopJson)) {
            return null;
        }

        // redis不存在 根据id查询数据库
        Shop shop = this.getById(id);
        // 数据库不存在 返回error
        if (shop == null) {
            // 将空值写入redis
            stringRedisTemplate.opsForValue().set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 数据库存在 将商铺写入redis 设置30分钟过期剔除
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop),
                RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 返回
        return shop;
    }

    /**
     * 获取锁
     *
     * @param key redis键
     * @return true false
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放锁
     *
     * @param key redis键
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 更新商铺
     *
     * @param shop 商铺信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShopById(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 更新数据库
        this.updateById(shop);
        // 删除缓存
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        stringRedisTemplate.delete(shopKey);
        return Result.ok();
    }
}
