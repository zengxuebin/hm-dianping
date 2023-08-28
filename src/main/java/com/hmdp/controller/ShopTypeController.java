package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 商铺分类控制层
 *
 * @author ZengXuebin
 * @since 2023/8/22 01:05
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Autowired
    private IShopTypeService typeService;

    /**
     * 查询商铺类别信息
     *
     * @return 商铺类别详情
     */
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
