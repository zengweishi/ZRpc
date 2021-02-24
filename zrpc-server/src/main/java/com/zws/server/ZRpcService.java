package com.zws.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/20 15:50
 * @Description:RPC远程服务注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ZRpcService {
    /**
     * 服务类
     * @return
     */
    Class<?> value();

    /**
     * 版本号
     * @return
     */
    String version() default "";
}
