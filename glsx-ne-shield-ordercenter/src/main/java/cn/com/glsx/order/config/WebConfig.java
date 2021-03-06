package cn.com.glsx.order.config;

import cn.com.glsx.auth.interceptor.WebFunctionPermissionInterceptor;
import cn.com.glsx.auth.interceptor.WebRequestAuthorizeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author payu
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private WebRequestAuthorizeInterceptor authInterceptor;

    @Resource
    private WebFunctionPermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //1.加入的顺序就是拦截器执行的顺序，
        //2.按顺序执行所有拦截器的preHandle
        //3.所有的preHandle 执行完再执行全部postHandle 最后是postHandle
        registry.addInterceptor(authInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/actuator/**")
                .excludePathPatterns("/api/**");

        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
    }

}
