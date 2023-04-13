package com.fit.common.interceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxInterceptor implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletReq, ServletResponse servletResp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletReq;
        HttpServletResponse response = (HttpServletResponse) servletResp;
        String ajaxSubmit = request.getHeader("X-Requested-With");
        if (ajaxSubmit != null && ajaxSubmit.equals("XMLHttpRequest") && request.getSession(false) == null) {
            response.setHeader("sessionStatus", "timeout");
            response.getWriter().print("sessionStatus");
        } else {
            chain.doFilter(servletReq, servletResp);
        }
    }

    public void destroy() {
    }
}