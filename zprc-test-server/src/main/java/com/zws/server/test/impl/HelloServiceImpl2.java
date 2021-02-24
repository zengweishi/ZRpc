package com.zws.server.test.impl;

import com.zws.server.ZRpcService;
import com.zws.server.test.HelloService;
import com.zws.server.test.Student;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 14:41
 * @Description:
 */
@ZRpcService(value = HelloService.class, version = "V2")
public class HelloServiceImpl2 implements HelloService {
    @Override
    public String hello(String msg) {
        return "It is V2，hello:" + msg;
    }

    @Override
    public String hello(Student student) {
        return "It is V2，hello:" + student.getId() + "-" +student.getName();
    }
}
