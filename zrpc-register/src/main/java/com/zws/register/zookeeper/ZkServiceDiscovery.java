package com.zws.register.zookeeper;

import com.alibaba.fastjson.JSON;
import com.zws.register.Constant;
import com.zws.register.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: weishi.zeng
 * @Date: 2021/2/22 18:25
 * @Description:ZK服务发现
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private String zkAddress;

    /**
     * spring配置文件中注入属性zkAddress
     * @param zkAddress
     */
    public ZkServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @Override
    public String disCovery(String name) {
        ZkClient zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECT_TIMEOUT);
        System.out.println("zk discovery connect success...");
        try {
            String servicePath = Constant.ZK_SERVER_REGISTER_PATH + "/" +name;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find service path:%s",servicePath));
            }
            List<String> children = zkClient.getChildren(servicePath);
            if (CollectionUtils.isEmpty(children)) {
                throw new RuntimeException(String.format("can not find any address node in %s",servicePath));
            }
            String address = null;
            if (children.size() == 1) {
                //只存在一个服务地址 E:\personalspace\PersonalProject\zrpc\zprc-test-server\target\zprc-test-server-1.0-SNAPSHOT.jar
                 System.out.println("exist one path, address path:"+children.get(0));
                address = children.get(0);
            } else {
                // 若存在多个地址（服务集群部署），则随机获取一个地址
                address = children.get(ThreadLocalRandom.current().nextInt(children.size()));
                 System.out.println("get random address path:"+address);
            }
            String addressPath = servicePath + "/" + address;
             System.out.println("address apth result:"+ JSON.toJSONString(addressPath));
            return zkClient.readData(addressPath);
        } catch (Exception e) {
            throw e;
        } finally {
            zkClient.close();
        }
    }
}
