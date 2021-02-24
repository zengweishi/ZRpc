package com.zws.server.test.impl;

import com.zws.server.ZRpcService;
import com.zws.server.test.HelloService;
import com.zws.server.test.Student;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 14:40
 * @Description:
 */
@ZRpcService(value = HelloService.class)
public class HelloServiceImpl1 implements HelloService {
    @Override
    public String hello(String msg) {
        return "hello:" + msg;
    }

    @Override
    public String hello(Student student) {
        return "hello:" + student.getId() + "-" +student.getName();
    }
}
