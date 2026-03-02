package com.resolver;

import com.annotation.LoginUser;
import com.entity.UserEntity;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * @LoginUser注解解析器
 */
@Component
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 支持UserEntity类型且有@LoginUser注解的参数
        return parameter.getParameterType().isAssignableFrom(UserEntity.class)
                && parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer container,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory factory) throws Exception {

        // 获取HttpServletRequest
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        // 从session中获取用户信息
        Long userId = (Long) request.getSession().getAttribute("userId");
        String username = (String) request.getSession().getAttribute("username");
        String role = (String) request.getSession().getAttribute("role");

        System.out.println("=== LoginUser解析器 ===");
        System.out.println("Session中的userId: " + userId);
        System.out.println("Session中的username: " + username);

        // 如果用户未登录，返回null
        if (userId == null) {
            System.out.println("用户未登录，返回null");
            return null;
        }

        // 创建并返回UserEntity对象
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setSname(username);
        user.setRole(role);

        System.out.println("创建的UserEntity - ID: " + user.getId() + ", Name: " + user.getSname());

        return user;
    }
}