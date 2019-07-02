package cn.shijinshi.fabricmanager.controller.interceptor;

import cn.shijinshi.fabricmanager.controller.annotation.CouchdbNodePermissionVerify;
import cn.shijinshi.fabricmanager.dao.CouchdbNodeService;
import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.CouchdbNode;
import cn.shijinshi.fabricmanager.exception.ServiceException;
import cn.shijinshi.fabricmanager.service.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

public class CouchdbNodePermissionVerifyInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private CouchdbNodeService couchdbService;
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
        if (method.isAnnotationPresent(CouchdbNodePermissionVerify.class)) {
            String userId = tokenManager.getRequester(request).getUserId();
            String couchdbName = (String) ((Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("couchdbName");

            CouchdbNode couchdbNode = couchdbService.selectByPrimaryKey(couchdbName);
            if (couchdbNode == null) {
                throw new ServiceException("未知的couchdb节点:"+couchdbName);
            }
            String creator = couchdbNode.getCreator();
            if (userService.isSelfOrChild(userId, creator)) {
                return true;
            } else {
                throw new ServiceException("用户" + userId + "无权请求/操作couchdb节点:" + couchdbName);
            }
        }

        return true;
    }
}
