package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商铺分类接口实现层
 *
 * @author ZengXuebin
 * @since 2023/8/22 01:05
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询商铺类别信息 采用list类型缓存
     *
     * @return 商铺类别详情
     */
    @Override
    public Result queryTypeList() {
        // 先查询redis是否命中
        List<String> shopTypeList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        if (shopTypeList != null && !shopTypeList.isEmpty()) {
            // redis命中 返回结果
            List<ShopType> typeList = shopTypeList.stream()
                    .map(shopType -> JSONUtil.toBean(shopType, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(typeList);
        }

        // redis未命中 向数据库请求数据
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        // 写入redis
        for (ShopType shopType : typeList) {
            stringRedisTemplate.opsForList().rightPush(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType));
        }
        return Result.ok(typeList);
    }
}
