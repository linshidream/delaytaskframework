package com.linshidream.delaytaskcore;

/**
 * Created on 2025/4/9 17:35
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 注册调度器
 */

public interface DelayTaskScheduler {

    /**
     * 启动某个 tag 的调度任务
     *
     * @param topic
     * @param tag
     * @param task
     * @param intervalMillis
     */
    void schedule(String topic, String tag, Runnable task, long intervalMillis);


    /**
     * 启动某个重试 tag 的调度任务
     *
     * @param topic
     * @param tag
     * @param task
     * @param intervalMillis
     */
    void retrySchedule(String topic, String tag, Runnable task, long intervalMillis);

    /**
     * 停止调度
     *
     * @param topic
     * @param tag
     */
    default void cancel(String topic, String tag) {
    }

    /**
     * 执行调度
     *
     * @param topic
     * @param tag
     */
    default void executeJob(String topic, String tag) {
    }

    /**
     * 执行重试调度
     *
     * @param topic
     * @param tag
     */
    default void executeRetryJob(String topic, String tag) {
    }
}
