package com.zrpc.common.bean;

import lombok.Data;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/23 14:58
 * @Description:RPC请求参数
 */
@Data
public class RpcRequest {
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 服务名称
     */
    private String interfaceName;

    /**
     * 版本
     */
    private String serviceVersion;

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数类型
     */
    private Class<?>[]parameterTypes;

    /**
     * 参数
     */
    private Object[] parameters;
}
