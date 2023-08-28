package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdGenerator;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 优惠券秒杀服务接口实现层
 *
 * @author ZengXuebin
 * @since 2023/8/28 22:43
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private RedisIdGenerator redisIdGenerator;

    /**
     * 秒杀优惠券
     *
     * @param voucherId 优惠券ID
     * @return res
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result seckillVoucher(Long voucherId) {
        // 查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        // 判断秒杀是否开始
        if (LocalDateTime.now().isBefore(voucher.getBeginTime())) {
            // 尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        // 判断秒杀是否结束
        if (LocalDateTime.now().isAfter(voucher.getEndTime())) {
            return Result.fail("秒杀已经结束！");
        }

        // 判断库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足！");
        }

        // 扣除库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).update();
        if (!success) {
            return Result.fail("库存不足！");
        }

        // 创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 订单ID
        long orderId = redisIdGenerator.generateNextId("order");
        voucherOrder.setId(orderId);
        // 用户ID
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        // 代金券ID
        voucherOrder.setVoucherId(voucherId);

        // 返回订单ID
        this.save(voucherOrder);
        return Result.ok(orderId);
    }
}
