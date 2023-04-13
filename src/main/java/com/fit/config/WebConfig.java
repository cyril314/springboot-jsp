package com.fit.config;

import com.fit.common.interceptor.ErrorInterceptor;
import com.fit.common.interceptor.LoginInterceptor;
import com.fit.common.listener.ConfigListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * @className: WebConfig
 * @description: WEB配置
 * @author: Aim
 * @date: 2023/4/11
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String VIEW_CONTENT_TYPE = "text/html;charset=UTF-8";
    @Value("${spring.mvc.view.prefix}")
    private String prefix;
    @Value("${spring.mvc.view.suffix}")
    private String suffix;

    @Autowired
    private ErrorInterceptor errorInterceptor;
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * ConfigListener注册
     */
    @Bean
    public ServletListenerRegistrationBean<ConfigListener> configListenerRegistration() {
        return new ServletListenerRegistrationBean(new ConfigListener());
    }

    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setCache(true);
        resolver.setPrefix(prefix);
        resolver.setSuffix(suffix);
        resolver.setExposeContextBeansAsAttributes(true);
        resolver.setContentType(VIEW_CONTENT_TYPE);
        return resolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(viewResolver());
        registry.enableContentNegotiation(new MappingJackson2JsonView());
    }

    /**
     * 访问根路径默认跳转 index.html页面 （简化部署方案： 可以把前端打包直接放到项目的 webapp，上面的配置）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("favicon.ico").addResourceLocations("classpath:/favicon.ico");
        registry.addResourceHandler("/html/**").addResourceLocations("classpath:/html/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/META-INF/resources/assets/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor);
        registry.addInterceptor(errorInterceptor);
    }
}
