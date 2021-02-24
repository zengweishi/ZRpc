package com.zws.register;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/22 18:12
 * @Description:注册中心接口
 */
public interface ServiceRegister {

    /**
     * 服务注册接口
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);

}
