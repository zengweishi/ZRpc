package com.zws.register.zookeeper;

import com.zws.register.Constant;
import com.zws.register.ServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/22 18:24
 * @Description:ZK服务注册
 */
@Slf4j
public class ZkServiceRegister implements ServiceRegister {
    private final ZkClient zkClient;

    /**
     * spring配置文件中注入zkAddress
     * @param zkAddress
     */
    public ZkServiceRegister(String zkAddress) {
        this.zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECT_TIMEOUT);
        System.out.println("zk register connect success...");
    }


    /**
     * 服务注册
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     * Zookeeper节点类型：https://blog.csdn.net/desilting/article/details/41043837
     */
    @Override
    public void register(String serviceName, String serviceAddress) {
        //创建/register节点（持久节点）
        String registerPath = Constant.ZK_SERVER_REGISTER_PATH;
        if (!zkClient.exists(registerPath)) {
            zkClient.createPersistent(registerPath);
             System.out.println("create register path:"+registerPath);
        }
        //创建/serverName节点（持久节点）
        String serverPath = registerPath + "/" + serviceName;
        if (!zkClient.exists(serverPath)) {
            zkClient.createPersistent(serverPath);
             System.out.println("create server path:" + serverPath);
        }
        //创建服务address节点（临时顺序节点）
        //临时顺序节点:在创建节点时，Zookeeper根据创建的时间顺序给该节点名称进行编号，就是zookeeper会在其名字后自动追加一个单调增长的数字后缀，作为新的节点名；
        //            当创建节点的客户端与zookeeper断开连接后，临时节点会被删除
        // example：/register/com.zws.server.test.HelloService/address-0000000001
        String addressPath = serverPath + "/address-";
        String addressRegisterPath = zkClient.createEphemeralSequential(addressPath, serviceAddress);
         System.out.println("create address path:"+ addressRegisterPath);
    }
}
