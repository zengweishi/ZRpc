package com.zws.server.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 15:03
 * @Description:启动服务，加载spring配置文件，启动rpc服务，注册服务到zk
 */
@Slf4j
public class RpcServerBootstrap {
    public static void main(String[] args) {
        System.out.println("server start...");
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
