package com.zws.register;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/22 18:30
 * @Description:服务发现接口
 */
public interface ServiceDiscovery {
    /**
     * 服务发现接口
     * @param name 服务名称
     */
    String disCovery(String name);
}
