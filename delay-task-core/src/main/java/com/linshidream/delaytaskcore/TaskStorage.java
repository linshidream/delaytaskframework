package com.linshidream.delaytaskcore;

import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;

import java.util.List;

/**
 * Created on 2025/4/9 13:39
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 任务存储
 */

public interface TaskStorage {

    /**
     * 保存任务
     *
     * @param topic topic
     * @param tag   tag
     * @param task  延时任务
     * @param <T>
     * @return 执行结果
     */
    <T extends BaseDelayMqMessage> SaveTaskResult addTask(String topic, String tag, T task);

    /**
     * 取出任务
     *
     * @param topic        topic
     * @param tag          tag
     * @param maxTimestamp 时间戳
     * @param maxCount     取出数量
     * @param clazz        取出对象
     * @param <T>
     * @return
     */
    <T extends BaseDelayMqMessage> List<T> pollExpiredTasks(String topic, String tag, long maxTimestamp, int maxCount, Class<T> clazz);


    /**
     * 回调通知任务执行结果
     *
     * @param topic   topic
     * @param tag     tag
     * @param results 执行结果
     * @param <T>
     */
    <T extends BaseDelayMqMessage> void onTaskExecuted(String topic, String tag, List<DelayTaskResult<T>> results);


    /**
     * 保存重试任务
     *
     * @param topic topic
     * @param tag   tag
     * @param task  延时任务
     * @param <T>
     * @return 执行结果
     */
    <T extends BaseDelayMqMessage> SaveTaskResult addRetryTask(String topic, String tag, T task);


    /**
     * 取出重试任务
     *
     * @param topic        topic
     * @param tag          tag
     * @param maxTimestamp 时间戳
     * @param maxCount     取出数量
     * @param clazz        取出对象
     * @param <T>
     * @return
     */
    <T extends BaseDelayMqMessage> List<T> pollExpiredRetryTasks(String topic, String tag, long maxTimestamp, int maxCount, Class<T> clazz);


    /**
     * 回调通知重试任务执行结果
     *
     * @param topic   topic
     * @param tag     tag
     * @param results 执行结果
     * @param <T>
     */
    <T extends BaseDelayMqMessage> void onRetryTaskExecuted(String topic, String tag, List<DelayTaskResult<T>> results);

}
