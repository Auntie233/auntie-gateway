package top.auntie.gateway.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfigure {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // #允许向该服务器提交请求的URI，*表示全部允许
        config.addAllowedOrigin(org.springframework.web.cors.CorsConfiguration.ALL);
        // 允许cookies跨域
        config.setAllowCredentials(true);
        // #允许访问的头信息,*表示全部
        config.addAllowedHeader(org.springframework.web.cors.CorsConfiguration.ALL);
        // 允许提交请求的方法，*表示全部允许
        config.addAllowedMethod(org.springframework.web.cors.CorsConfiguration.ALL);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 配置过滤器
     */
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(corsFilter());
        registration.addUrlPatterns("/*");
        registration.setName("corsFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
