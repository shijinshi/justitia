package cn.shijinshi.fabricmanager.controller.interceptor;

import cn.shijinshi.fabricmanager.controller.annotation.CaServerPermissionVerify;
import cn.shijinshi.fabricmanager.controller.annotation.CaUserPermissionVerify;
import cn.shijinshi.fabricmanager.dao.FabricCaServerService;
import cn.shijinshi.fabricmanager.dao.FabricCaUserService;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaServer;
import cn.shijinshi.fabricmanager.dao.entity.FabricCaUser;
import cn.shijinshi.fabricmanager.dao.exception.NotFoundBySqlException;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 验证请求用户是否有权限使用CA服务或CA用户
 */
@Component
public class CAPermissionVerifyInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private FabricCaServerService serverService;
    @Autowired
    private FabricCaUserService caUserService;
    @Autowired
    private TokenManager tokenManager;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        //如果不是映射方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        return caServerPermissionVerify(request, method) && caUserPermissionVerify(request, method);
    }

    /**
     * 校验请求用户是否有权限操作指定的fabric ca server
     */
    private boolean caServerPermissionVerify(HttpServletRequest request, Method method) {
        //使用了CaServerPermissionVerify注解的方法需要被校验
        if (method.isAnnotationPresent(CaServerPermissionVerify.class)) {
            String userId = tokenManager.getRequester(request).getUserId();
            String serverName = (String) ((Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("serverName");
            FabricCaServer caServer;
            try {
                caServer = serverService.getServer(serverName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("不存在名称为"+serverName +"的CA服务");
            }
            String creator = caServer.getCreator();
            if (userService.isSelfOrChild(userId, creator)) {
                return true;
            } else {
                throw new ServiceException("用户" + userId + "无权请求/操作" + serverName);
            }
        } else {
            return true;
        }
    }


    /**
     * 校验请求用户是否有权限使用指定的CA user
     */
    private boolean caUserPermissionVerify(HttpServletRequest request, Method method) {
        if (method.isAnnotationPresent(CaUserPermissionVerify.class)) {
            String userId = tokenManager.getRequester(request).getUserId();

            String serverName = (String) ((Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("serverName");
            String caUserId = (String) ((Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("caUserId");
            FabricCaUser caUser;
            try {
                caUser = caUserService.getCaUser(caUserId, serverName);
            } catch (NotFoundBySqlException e) {
                throw new ServiceException("不存在的CA用户" + serverName + ":" + caUserId);
            }

            String caUserOwner = caUser.getOwner();
            if (userService.isSelfOrChild(userId, caUserOwner)) {
                return true;
            } else {
                throw new ServiceException("无权使用" + serverName + "服务中" + caUserId + "用户进行请求。");
            }
        } else {
            return true;
        }
    }
}
