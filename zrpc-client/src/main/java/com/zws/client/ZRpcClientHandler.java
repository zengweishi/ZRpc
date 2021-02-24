package com.zws.client;

import com.alibaba.fastjson.JSON;
import com.zrpc.common.bean.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/23 16:44
 * @Description:RPC响应处理类
 * 服务端用ChannelInboundHandlerAdapter
 * 客户端用SimpleChannelInboundHandler
 * SimpleChannelInboundHandler接收到数据后会自动release掉数据占用的Bytebuffer资源(自动调用Bytebuffer.release())，并且入站的消息可以通过泛型来规定。
 * 服务端需要将传入消息回送给发送者，而 write() 操作是异步的，直到 channelRead() 方法返回后可能仍然没有完成
 * ChannelInboundHandlerAdapter不会像SimpleChannelInboundHandler一样在 channelRead()里面释放资源，而是在收到消息处理完成的事件时，才会释放资源
 */
@Slf4j
public class ZRpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private RpcResponse response;

    public ZRpcClientHandler(RpcResponse response) {
        this.response = response;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
         System.out.println("ZRpcClientHandler rpcResponse:" + JSON.toJSONString(rpcResponse));
        this.response = rpcResponse;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         System.out.println("rpc client exception...");
        ctx.close();
    }
}
