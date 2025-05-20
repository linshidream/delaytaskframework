package com.linshidream.delaytaskcore.distributor;

import java.util.List;

/**
 * Created on 2025/4/14 17:06
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 任务节点分配策略
 */

public interface DelayTaskAssignStrategy {


    /**
     * 唯一策略标识（如 ROUND、FIRST 等）
     *
     * @return
     */
    String strategy();

    /**
     * 判断当前实例是否符合当前策略，如果不符合，则返回fasle。
     * 例如 "LSAT" 要求最后一个实例执行，当前实例属于最后一个实例，返回true
     *
     * @param topic
     * @param tag
     * @param instanceId
     * @param healthyInstances 所以健康节点
     * @return
     */
    boolean shouldSchedule(String topic, String tag, String instanceId, List<String> healthyInstances);
}
