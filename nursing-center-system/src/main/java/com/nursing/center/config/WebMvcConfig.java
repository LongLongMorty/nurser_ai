package com.nursing.center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.config
 * @Author: System
 * @CreateTime: 2025-01-21
 * @Description: Web MVC 配置类，用于统一处理API路径前缀
 * @Version: 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配，为所有控制器添加全局API前缀
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", c -> 
            c.getPackageName().startsWith("com.nursing.center.controller") &&
            !c.getName().equals("StaticController") // 排除静态资源控制器
        );
    }
}
