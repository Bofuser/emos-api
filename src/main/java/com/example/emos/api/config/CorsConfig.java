package com.example.emos.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 这段Java代码定义了一个名为CorsConfig的类，它实现了WebMvcConfigurer接口，并使用@Configuration注解进行了标记。CorsConfig类的主要作用是配置跨域资源共享（CORS）。
 * 在Web开发中，CORS是一种机制，它允许Web应用程序在浏览器中访问不同源的资源。CORS机制通过在服务器端设置HTTP响应头来实现。在Spring框架中，可以通过CorsConfig类来配置CORS。
 * CorsConfig类中的addCorsMappings方法用于配置CORS映射。在这个方法中，我们可以指定允许跨域访问的路径、允许的请求方法、允许的请求头等信息。具体来说，这个方法中的代码：
 * registry.addMapping("/**")：表示允许所有路径的资源进行跨域访问。
 * .allowedOriginPatterns("*")：表示允许所有来源的请求进行跨域访问。
 * .allowCredentials(true)：表示允许发送cookie等凭证信息。
 * .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH")：表示允许的请求方法。
 * .maxAge(3600)：表示在指定时间内，不需要再发送预检请求，单位为秒。
 * 通过配置CorsConfig类，我们可以实现跨域资源共享，从而让Web应用程序能够在浏览器中访问不同源的资源。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH")
                .maxAge(3600);
    }
}