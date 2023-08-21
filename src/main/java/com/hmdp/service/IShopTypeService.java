package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商铺分类服务接口层
 *
 * @author ZengXuebin
 * @since 2023/8/22 01:05
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * 查询商铺类别信息
     *
     * @return 商铺类别详情
     */
    Result queryTypeList();
}
