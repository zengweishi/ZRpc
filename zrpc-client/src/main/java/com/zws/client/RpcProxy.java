package com.zws.client;

import com.zrpc.common.bean.RpcRequest;
import com.zrpc.common.bean.RpcResponse;
import com.zrpc.common.utils.StringUtil;
import com.zws.register.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/23 15:41
 * @Description:RPC代理，创建RPC服务代理
 */
@Slf4j
public class RpcProxy {
    private ServiceDiscovery serviceDiscovery;

    private String serviceAddress;

    /**
     * spring配置文件中注入属性
     * @param serviceDiscovery
     */
    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     *  创建服务的动态代理对象
     *  <T> T表示返回值是一个泛型，传递啥，就返回啥类型的数据
     */
    public <T> T create(Class<?> interfaceClass) {
        System.out.println("proxy create1");
        return create(interfaceClass,"");
    }

    /**
     * 创建服务的动态代理对象
     * @param interfaceClass
     * @param serviceVersion
     * @param <T>
     * @return
     */
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        System.out.println("proxy create2");
        /**
         *  newProxyInstance方法的参数：
         *     ClassLoader：类加载器
         *          它是用于加载代理对象字节码的。和【被代理对象】使用相同的类加载器。固定写法。
         *     Class[]：字节码数组
         *          它是用于让代理对象和被代理对象有相同方法。固定写法。
         *     InvocationHandler：用于提供增强的代码
         *          它是让我们写如何代理。我们一般都是些一个该接口的实现类，通常情况下都是匿名内部类，但不是必须的。
         *          此接口的实现类都是谁用谁写。
         */
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    /**
                     * //增强方法，代理对象执行的方法
                     * 作用：执行【被代理对象】的任何接口方法都会经过该方法
                     * @param proxy   代理对象的引用
                     * @param method  当前执行的方法
                     * @param args    当前执行方法所需的参数
                     * @return        和被代理对象方法有相同的返回值
                     * @throws Throwable
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setMethod(method.getName());
                        request.setParameters(args);
                        request.setParameterTypes(method.getParameterTypes());
                        request.setServiceVersion(serviceVersion);

                        //从注册中心获取服务地址
                        if (serviceDiscovery != null) {
                            String serverName = interfaceClass.getName();
                            if (StringUtil.isNotEmpty(serviceVersion)) {
                                serverName += "-" + serviceVersion;
                            }
                            serviceAddress = serviceDiscovery.disCovery(serverName);
                             System.out.println("service address:" + serviceAddress);
                        }
                        if (StringUtil.isEmpty(serviceAddress)) {
                            throw new RuntimeException("service address is empty");
                        }
                        //解析主机名和端口号
                        String[] address = StringUtil.split(serviceAddress,":");
                        System.out.println(serviceAddress);
                        String host = address[0];
                        int port = Integer.valueOf(address[1]);
                        ZRpcClient client = new ZRpcClient(host, port);
                        long time = System.currentTimeMillis();
                        //发送请求
                        RpcResponse response = client.send(request);
                         System.out.println("times:" + (System.currentTimeMillis() - time) + "ms");

                        if (response == null) {
                            throw new RuntimeException("response is null");
                        } else if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }
}
