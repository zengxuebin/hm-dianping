package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品服务接口层
 *
 * @author ZengXuebin
 * @since 2023/8/22 00:40
 */
public interface IShopService extends IService<Shop> {

    /**
     * 查询商铺
     *
     * @param id 商铺id
     * @return 商铺详情数据
     */
    Result queryShopById(Long id);

    /**
     * 更新商铺
     * @param shop 商铺信息
     * @return 结果
     */
    Result updateShopById(Shop shop);
}
