package cn.shijinshi.fabricmanager.controller.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /*
          addPathPatterns          添加拦截规则
          excludePathPatterns      排除拦截
         */
        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/**");     //拦截所有

        registry.addInterceptor(caPermissionVerifyInterceptor())
                .addPathPatterns("/ca/**");

//        registry.addInterceptor(couchdbNodePermissionVerifyInterceptor())
//                .addPathPatterns("//node/couchdb/**");
    }

    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }

    @Bean
    public CAPermissionVerifyInterceptor caPermissionVerifyInterceptor() {
        return new CAPermissionVerifyInterceptor();
    }

    @Bean
    public CouchdbNodePermissionVerifyInterceptor couchdbNodePermissionVerifyInterceptor() {
        return new CouchdbNodePermissionVerifyInterceptor();
    }
}
