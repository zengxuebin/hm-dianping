package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * 用户服务接口层
 *
 * @author ZengXuebin
 * @since 2023-08-20 20:05
 */
public interface IUserService extends IService<User> {

    /**
     * 发送短信验证码
     * @param phone   手机号
     * @param session 会话
     * @return 验证码
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 登录
     * @param loginForm 登录表单
     * @param session   会话
     * @return res
     */
    Result login(LoginFormDTO loginForm, HttpSession session);
}
