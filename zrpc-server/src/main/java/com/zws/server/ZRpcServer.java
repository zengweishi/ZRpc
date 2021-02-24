package com.zws.server;

import com.zrpc.common.bean.RpcRequest;
import com.zrpc.common.bean.RpcResponse;
import com.zrpc.common.codec.RpcDecoder;
import com.zrpc.common.codec.RpcEncoder;
import com.zrpc.common.utils.StringUtil;
import com.zws.register.ServiceRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/20 16:26
 * @Description:RPC服务器
 */
@Slf4j
public class ZRpcServer implements ApplicationContextAware, InitializingBean {
    /**
     * 存放服务名称和对象的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<>();

    /**
     * 服务器地址 127.0.0.1:8000
     */
    private String serviceAddress;
    /**
     * 注册中心接口
     */
    private ServiceRegister serviceRegister;

    /**
     * spring配置文件中注入属性
     * @param serviceAddress
     * @param serviceRegister
     */
    public ZRpcServer(String serviceAddress, ServiceRegister serviceRegister) {
        this.serviceAddress = serviceAddress;
        this.serviceRegister = serviceRegister;
    }

    /**
     * 从ApplicationContext获取带有rpc注解的Bean,进而初始化属性handlerMap
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取带有ZRpcService的Bean
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(ZRpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object bean : serviceBeanMap.values()) {
                ZRpcService service = bean.getClass().getAnnotation(ZRpcService.class);
                //服务名称
                String serviceName = service.value().getName();
                //版本号
                String serviceVersion = service.version();
                if (StringUtil.isNotEmpty(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                //保存服务名称和Bean对象的映射 ["helloService-v1", HelloServiceImpl.class]
                handlerMap.put(serviceName,bean);
            }
        }
    }

    /**
     * 属性注入完后执行
     * https://blog.csdn.net/maclaren001/article/details/37039749
     * 1.启动RPC服务器接收客户端rpc请求
     * 2.注册rpc服务到服务注册中心
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            //创建并初始化Netty服务端BootStrap对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //绑定线程组
            serverBootstrap.group(bossGroup,workGroup);
            //设置服务端的通道实现
            serverBootstrap.channel(NioServerSocketChannel.class);
            //设置业务处理类
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //解码器，解码RPC请求
                    pipeline.addLast(new RpcDecoder(RpcRequest.class));
                    //编码器，编码RPC响应
                    pipeline.addLast(new RpcEncoder(RpcResponse.class));
                    //RPC请求业务处理类
                    pipeline.addLast(new ZRpcServerHandler(handlerMap));
                }
            });
            //设置服务端通道的选项参数
            //ChannelOption.SO_BACKLOG 初始化服务器端可连接队列大小，不能处理的连接请求放在队列等待处理
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            //设置客户端连接SocketChannel通道参数
            //ChannelOption.SO_KEEPALIVE 一直保持连接状态
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            //获取RPC服务器的地址(IP+端口)
            String[] addressArr = StringUtil.split(serviceAddress, ":");
            String host = addressArr[0];
            int port = Integer.parseInt(addressArr[1]);
            //1.启动rpc服务器
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            //2.注册服务地址到注册中心 zk
            if (serviceRegister != null) {
                for (String serviceName : handlerMap.keySet()) {
                    serviceRegister.register(serviceName, serviceAddress);
                }
            }

            //关闭rpc服务器
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            throw e;
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
