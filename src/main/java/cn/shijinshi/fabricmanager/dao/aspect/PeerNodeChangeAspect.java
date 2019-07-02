package cn.shijinshi.fabricmanager.dao.aspect;

import cn.shijinshi.fabricmanager.Context;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class PeerNodeChangeAspect {

    @Pointcut("execution(public int cn.shijinshi.fabricmanager.dao.PeerNodeService.deleteByPrimaryKey(..))" +
            "|| execution(public int cn.shijinshi.fabricmanager.dao.PeerNodeService.insertSelective(..))")
    public void peerCountChange(){}

    @Around("peerCountChange()")
    public Object aroundPeerCountChange(ProceedingJoinPoint joinPoint) throws Throwable {
        int res = (int) joinPoint.proceed();
        if (res != 0) {
            Context.loadPeerInfo();
        }
        return res;
    }
}
