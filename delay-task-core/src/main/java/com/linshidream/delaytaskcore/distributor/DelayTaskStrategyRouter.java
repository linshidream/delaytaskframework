package com.linshidream.delaytaskcore.distributor;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.List;

/**
 * Created on 2025/4/14 17:08
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 策略路由器
 */

@Component
public class DelayTaskStrategyRouter {

    @Resource
    private DelayTaskInstanceRegistry delayTaskInstanceRegistry;

    @Resource
    private Map<String, DelayTaskAssignStrategy> delayTaskAssignStrategyMap;

    /**
     * 通过此路由层，匹配对应策略并判断当前实例是否具备执行条件
     *
     * @param topic
     * @param tag
     * @param strategyType
     * @return
     */
    public boolean shouldSchedule(String topic, String tag, String strategyType) {
        String registryAddr = delayTaskInstanceRegistry.getRegistryAddr();
        String instanceId = InstanceIdGenerator.generate(registryAddr);
        List<String> instances = delayTaskInstanceRegistry.getOrderedHealthyInstancesAndCleanup(topic, tag);
        if (instances == null || instances.size() == 0) {
            return false;
        }
        DelayTaskAssignStrategy strategy = delayTaskAssignStrategyMap.get(strategyType);
        return strategy != null && strategy.shouldSchedule(topic, tag, instanceId, instances);
    }

}
