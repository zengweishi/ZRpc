package com.zws.server.test;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 14:39
 * @Description:
 */
public interface HelloService {
    String hello(String msg);

    String hello(Student student);
}
