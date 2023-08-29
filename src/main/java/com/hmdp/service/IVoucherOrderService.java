package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 优惠券秒杀服务接口层
 *
 * @author ZengXuebin
 * @since 2023/8/28 22:42
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 秒杀优惠券
     *
     * @param voucherId 优惠券ID
     * @return res
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 并发创建订单
     *
     * @param voucherId 优惠券ID
     * @return res
     */
    Result createVoucherOrder(Long voucherId);
}
