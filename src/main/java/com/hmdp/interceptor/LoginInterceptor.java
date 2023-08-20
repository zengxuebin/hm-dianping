package com.hmdp.interceptor;

import com.hmdp.constant.SessionKeyConstant;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @author ZengXuebin
 * @since 2023/8/20 23:40
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取session中的用户
        UserDTO user = (UserDTO) request.getSession().getAttribute(SessionKeyConstant.USER);
        // 判断用户是否存在
        // 不存在 拦截
        if (user == null) {
            // 返回401状态码 未授权
            response.setStatus(401);
            return false;
        }
        // 存在 保存用户信息到ThreadLocal
        UserHolder.saveUser(user);
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
