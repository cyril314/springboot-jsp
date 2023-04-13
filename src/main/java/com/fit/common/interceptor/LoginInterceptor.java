package com.fit.common.interceptor;

import com.fit.common.Constants;
import com.fit.common.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @className: LoginInterceptor
 * @description: 登录拦截器
 * @author: Aim
 * @date: 2023/4/13
 **/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler, ModelAndView mav) throws Exception {
        HttpSession session = req.getSession(true);
        String username = (String) session.getAttribute(Constants.SESSION_USER_NAME);
        if (StringUtil.isEmpty(username)) {
            mav.setViewName("system/login");
            req.setAttribute("message", "未登录或登录超时!");
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) throws Exception {
    }
}
