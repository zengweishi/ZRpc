package com.zws.server.test;

import lombok.Data;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/24 14:40
 * @Description:
 */
@Data
public class Student {
    private Integer id;
    private String name;

    public Student(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
