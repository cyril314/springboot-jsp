package com.fit.common.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @className: ConfigListener
 * @description: ServletContext监听器
 * @author: Aim
 * @date: 2023/4/11
 **/
public class ConfigListener implements ServletContextListener {

    private static Map<String, String> conf = new HashMap<>();

    public static Map<String, String> getConf() {
        return conf;
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        conf.clear();
    }

    @Override
    public void contextInitialized(ServletContextEvent evt) {
        ServletContext sc = evt.getServletContext();
        //项目发布,当前运行环境的绝对路径
        conf.put("realPath", sc.getRealPath("/").replaceFirst("/", ""));
        //servletContextPath,默认""
        conf.put("contextPath", sc.getContextPath());
        conf.put("ctx", sc.getContextPath());
    }
}
