package com.zws.test.client;

import com.zws.client.RpcProxy;
import com.zws.server.test.HelloService;
import com.zws.server.test.Student;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 15:50
 * @Description:模拟客户端
 */
public class HelloClient {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);

        HelloService helloService = rpcProxy.create(HelloService.class);
        String result1 = helloService.hello("client1");
        String result2 = helloService.hello(new Student(1,"tom1"));
        System.out.println("result1：" + result1);
        System.out.println("result2：" + result2);

        HelloService helloService_V2 = rpcProxy.create(HelloService.class, "V2");
        String result3 = helloService_V2.hello("client_V2");
        String result4 = helloService_V2.hello(new Student(2, "tom2"));
        System.out.println("result3：" + result3);
        System.out.println("result4：" + result4);
    }
}
