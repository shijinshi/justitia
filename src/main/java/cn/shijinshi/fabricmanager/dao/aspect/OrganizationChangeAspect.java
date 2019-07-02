package cn.shijinshi.fabricmanager.dao.aspect;

import cn.shijinshi.fabricmanager.Context;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class OrganizationChangeAspect {


    @Pointcut("execution(public int cn.shijinshi.fabricmanager.dao.OrganizationService.insertSelective(..))" +
            "|| execution(public int cn.shijinshi.fabricmanager.dao.OrganizationService.deleteOrganization(..))")
    public void organizationChange(){}


    @Around("organizationChange()")
    public Object aroundOrganizationChange(ProceedingJoinPoint joinPoint) throws Throwable {
        int res = (int)joinPoint.proceed();
        if (res != 0) {
            Context.resetContext();
        }
        return res;
    }
}
