package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.RedisConstants;
import com.hmdp.constant.SystemConstant;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务接口实现层
 *
 * @author ZengXuebin
 * @since 2023-08-20 20:05
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 验证码
     */
    @Override
    public Result sendCode(String phone) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不符合 返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 符合 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 保存验证码至redis 验证码有效期两分钟
        String codeKey = RedisConstants.LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 发送验证码
        log.debug("您的验证码为：{}，请勿泄露于他人！", code);
        // 返回ok
        return Result.ok();
    }

    /**
     * 登录
     *
     * @param loginForm 登录表单
     * @return res
     */
    @Override
    public Result login(LoginFormDTO loginForm) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不符合 返回错误信息
            return Result.fail("手机号格式错误");
        }

        // 从redis取出验证码 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        // 不一致 报错
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }
        // 一致 根据手机号查询用户
        User user = lambdaQuery().eq(User::getPhone, phone).one();

        // 判断用户是否存在
        // 不存在 创建新用户并保存
        if (user == null) {
            user = createWithPhone(phone);
        }

        // 随机生成token 作为登录令牌
        // true可以去除短线
        String token = UUID.randomUUID().toString(true);

        // 将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create()
                // 忽略空值
                .setIgnoreNullValue(true)
                // 将value转换为string
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        // 存储至redis Hash存储对象
        String tokenKey = RedisConstants.LOGIN_TOKEN_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置token有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_TOKEN_TTL, TimeUnit.MILLISECONDS);

        return Result.ok(token);
    }

    private User createWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        String nickname = SystemConstant.NICKNAME_PREFIX + RandomUtil.randomString(10);
        user.setNickName(nickname);
        save(user);
        return user;
    }
}
