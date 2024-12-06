package com.fit.config;

import com.fit.common.interceptor.ActionInterceptor;
import com.fit.common.listener.ConfigListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Value("${spring.mvc.view.prefix}")
    private String prefix;
    @Value("${spring.mvc.view.suffix}")
    private String suffix;

    @Autowired
    private ActionInterceptor actionInterceptor;

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setCache(true);
        resolver.setPrefix(prefix);
        resolver.setSuffix(suffix);
        resolver.setExposeContextBeansAsAttributes(true);
        resolver.setContentType("text/html;charset=UTF-8");
        registry.viewResolver(resolver);
        registry.enableContentNegotiation(new MappingJackson2JsonView());
    }

    /**
     * 访问根路径默认跳转 index.html页面
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    /**
     * 添加静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/META-INF/resources/assets/");
        registry.addResourceHandler("/html/**").addResourceLocations("classpath:/html/");
        registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("favicon.ico").addResourceLocations("classpath:/favicon.ico");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(actionInterceptor).addPathPatterns("/**").excludePathPatterns("/assets/**");
    }

    /**
     * ConfigListener注册
     */
    @Bean
    public ServletListenerRegistrationBean<ConfigListener> configListenerRegistration() {
        return new ServletListenerRegistrationBean(new ConfigListener());
    }
}