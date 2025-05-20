package com.linshidream.delaytaskcore;


import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;

import java.util.List;

/**
 * Created on 2025/4/11 19:09
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 死信消息
 */

public interface DeadTaskStorage {

    /**
     * 保存一批死信任务
     *
     * @param topic
     * @param tag
     * @param taskList
     * @param <T>
     */
    <T extends BaseDelayMqMessage> void save(String topic, String tag, List<T> taskList);

    /**
     * 查询指定时间范围内的死信任务
     *
     * @param topic
     * @param tag
     * @param startTimeMillis
     * @param endTimeMillis
     * @return
     */
    List<String> query(String topic, String tag, long startTimeMillis, long endTimeMillis);

    /**
     * 删除某些死信任务, 用于补偿成功后的回调
     *
     * @param topic
     * @param tag
     * @param messageBodies
     */
    void remove(String topic, String tag, List<String> messageBodies);

    /**
     * 清理某个时间之前的死信消息
     *
     * @param topic
     * @param tag
     * @param beforeTimestamp
     */
    void clearBefore(String topic, String tag, long beforeTimestamp);

    /**
     * 定期清理任务，由定时任务驱动
     */
    void scheduledCleanup();

}
