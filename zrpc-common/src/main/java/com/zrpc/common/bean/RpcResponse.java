package com.zrpc.common.bean;

import lombok.Data;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/23 14:58
 * @Description:RPC响应结果
 */
@Data
public class RpcResponse {
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 结果
     */
    private Object result;
    /**
     * 异常
     */
    private Exception exception;

    public Boolean hasException() {
        return exception != null;
    }
}
