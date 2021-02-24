package com.zws.client;

import com.alibaba.fastjson.JSON;
import com.zrpc.common.bean.RpcRequest;
import com.zrpc.common.bean.RpcResponse;
import com.zrpc.common.codec.RpcDecoder;
import com.zrpc.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/23 15:40
 * @Description:RPC客户端
 * 服务端用ChannelInboundHandlerAdapter
 *   客户端用SimpleChannelInboundHandler
 *   SimpleChannelInboundHandler接收到数据后会自动release掉数据占用的Bytebuffer资源(自动调用Bytebuffer.release())，并且入站的消息可以通过泛型来规定。
 *   服务端需要将传入消息回送给发送者，而 write() 操作是异步的，直到 channelRead() 方法返回后可能仍然没有完成
 *   ChannelInboundHandlerAdapter不会像SimpleChannelInboundHandler一样在 channelRead()里面释放资源，而是在收到消息处理完成的事件时，才会释放资源
 */
@Slf4j
public class ZRpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private String host;
    private int port;
    private RpcResponse response;

    public ZRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException {
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //编码器 编码RPC请求
                    pipeline.addLast(new RpcEncoder(RpcRequest.class));
                    //解码器 解码RPC请求
                    pipeline.addLast(new RpcDecoder(RpcResponse.class));
                    //RPC响应处理类
                    pipeline.addLast(ZRpcClient.this);
                }
            });
            //TCP_NODELAY就是用于启用或关于Nagle算法。
            //如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；
            //如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送.
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            //连接rpc服务器
            ChannelFuture future = bootstrap.connect(host,port).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            //返回rpc对象
             System.out.println("ZRpcClient response:" +  JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            throw e;
        } finally {
            loopGroup.shutdownGracefully();
        }
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
