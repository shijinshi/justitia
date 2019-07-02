package cn.shijinshi.fabricmanager.controller.interceptor;

import cn.shijinshi.fabricmanager.controller.annotation.PassIdentityVerify;
import cn.shijinshi.fabricmanager.service.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //请求跨域
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods", "GET,HEAD,PUT,POST,DELETE,OPTIONS,PATCH");
        response.setHeader("Access-control-Allow-Headers", "Content-Type,authorization");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Max-Age","3600");

        //如果不是映射方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }




        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(PassIdentityVerify.class)) { //检查是否有PassIdentityVerify(跳过token校验)注解
            PassIdentityVerify passIdentityVerify = method.getAnnotation(PassIdentityVerify.class);
            if (passIdentityVerify.required()) {
                return true;
            }
        }
        return tokenManager.verifyToken(request);
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
    }
}
