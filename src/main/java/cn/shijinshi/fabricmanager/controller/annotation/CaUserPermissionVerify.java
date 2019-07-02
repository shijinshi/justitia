package cn.shijinshi.fabricmanager.controller.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注解的controller方法会验证是否有权使用指定的CA用户
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CaUserPermissionVerify {
}
