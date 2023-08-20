package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.SystemConstant;
import com.hmdp.constant.SessionKeyConstant;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * 用户服务接口实现层
 *
 * @author ZengXuebin
 * @since 2023-08-20 20:05
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 发送短信验证码
     *
     * @param phone   手机号
     * @param session 会话
     * @return 验证码
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不符合 返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 符合 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码至session
        session.setAttribute(SessionKeyConstant.CODE, code);
        // 发送验证码
        log.debug("您的验证码为：{}，请勿泄露于他人！", code);
        // 返回ok
        return Result.ok();
    }

    /**
     * 登录
     *
     * @param loginForm 登录表单
     * @param session   会话
     * @return res
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不符合 返回错误信息
            return Result.fail("手机号格式错误");
        }

        // 校验验证码
        String cacheCode = (String) session.getAttribute(SessionKeyConstant.CODE);
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

        // 保存用户信息至session
        session.setAttribute(SessionKeyConstant.USER, BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
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
