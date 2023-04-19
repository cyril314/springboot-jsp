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

    //不对匹配该值的访问路径拦截（正则）
    public static final String NO_INTERCEPTOR_PATH = ".*/((login)|(logout)|(code)|(app)|(weixin)|(assets)|(main)|(websocket)).*";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse resp, Object handler, ModelAndView mav) throws Exception {
        String path = req.getServletPath();
        if (!path.matches(NO_INTERCEPTOR_PATH)) {
            HttpSession session = req.getSession(true);
            String username = (String) session.getAttribute(Constants.SESSION_USER_NAME);
            if (StringUtil.isEmpty(username)) {
                mav = new ModelAndView("system/login");
                mav.addObject("message", "未登录或登录超时!");
            }
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) throws Exception {
    }
}
