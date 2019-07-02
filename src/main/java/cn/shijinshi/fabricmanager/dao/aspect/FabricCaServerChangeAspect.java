package cn.shijinshi.fabricmanager.dao.aspect;


import cn.shijinshi.fabricmanager.Context;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class FabricCaServerChangeAspect {

    @Pointcut("execution(public * cn.shijinshi.fabricmanager.dao.FabricCaServerService.deleteServerByName(..))")
    public void deleteServer(){}

    @Around("deleteServer()")
    public Object afterDeleteServer(ProceedingJoinPoint joinPoint) throws Throwable {
        Object res = joinPoint.proceed();
        Object[] args = joinPoint.getArgs();
        if (args.length == 1) {
            String serverName = (String) args[0];
            Context.delTlsServer(serverName);
        }
        return res;

    }
}
