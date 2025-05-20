package com.linshidream.delaytaskcore.distributor;

import java.util.List;

/**
 * Created on 2025/4/14 18:05
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 注册中心
 */

public interface DelayTaskInstanceRegistry {

    /**
     * 获取当前注册中心实例IP端口
     *
     * @return
     */
    String getRegistryAddr();


    /**
     * 注册当前实例
     *
     * @param topic
     * @param tag
     * @param instanceId
     */
    void registerInstance(String topic, String tag, String instanceId);

    /**
     * 注销当前实例
     *
     * @param topic
     * @param tag
     * @param instanceId
     */
    void unregisterInstance(String topic, String tag, String instanceId);

    /**
     * 心跳检查
     *
     * @param topic
     * @param tag
     * @param instanceId
     */
    void instanceHeartbeat(String topic, String tag, String instanceId);


    /**
     * 按注册顺序获取健康实例并清理异常节点
     *
     * @param topic
     * @param tag
     * @return
     */
    List<String> getOrderedHealthyInstancesAndCleanup(String topic, String tag);
}
