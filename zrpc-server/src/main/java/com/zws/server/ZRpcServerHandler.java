package com.zws.server;

import com.zrpc.common.bean.RpcRequest;
import com.zrpc.common.bean.RpcResponse;
import com.zrpc.common.utils.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/22 18:00
 * @Description:RPC请求业务处理类
 * 服务端用ChannelInboundHandlerAdapter
 * 客户端用SimpleChannelInboundHandler
 *  SimpleChannelInboundHandler接收到数据后会自动release掉数据占用的Bytebuffer资源(自动调用Bytebuffer.release())，并且入站的消息可以通过泛型来规定。
 *  服务端需要将传入消息回送给发送者，而 write() 操作是异步的，直到 channelRead() 方法返回后可能仍然没有完成
 *  ChannelInboundHandlerAdapter不会像SimpleChannelInboundHandler一样在 channelRead()里面释放资源，而是在收到消息处理完成的事件时，才会释放资源
 */
@Slf4j
public class ZRpcServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 存放服务名称和对象的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<>();

    public ZRpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest)msg;
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handler(request);
                response.setResult(result);
            } catch (Exception e) {
                response.setException(e);
            }
            //写入rpc响应对象并关闭连接
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private Object handler(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //获取服务名称
        String serviceName = request.getInterfaceName();
        //获取服务版本
        String serviceVersion = request.getServiceVersion();
        if (StringUtil.isNotEmpty(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }
        Object service = handlerMap.get(serviceName);
        if (service == null) {
            throw new RuntimeException(String.format("can not find service: %s",serviceName));
        }
        //获取反射调用所需参数：方法参数类型，方法参数，方法名，Bean
        Class<?> serviceClass = service.getClass();
        //方法名
        String methodName = request.getMethod();
        //方法参数类型
        Class<?>[] parameterTypes = request.getParameterTypes();
        //方法参数
        Object[] parameters = request.getParameters();

        //执行反射调用方法
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        //setAccessible是启用和禁用访问安全检查的开关，通过setAccessible(true)的方式关闭安全检查就可以达到提升反射速度的目的
        method.setAccessible(true);
        return method.invoke(service, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         System.out.println("zrpchandler exception...");
        ctx.close();
    }
}
