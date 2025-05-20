package com.linshidream.delaytaskcore;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2025/4/9 17:56
 *
 * @author zhengxing
 * @version 1.0.0
 * @description xxl 或者外部调度器
 */

@Slf4j
public class XxlJobDelayTaskScheduler implements DelayTaskScheduler {


    private final Map<String, Runnable> taskMap = new ConcurrentHashMap<>();

    /**
     * 每个任务对应的时间 ，<后期建议自动注册到xxl>, 不需要手动添加任务
     */
    private final Map<String, Long> taskIntervalMap = new ConcurrentHashMap<>();

    @Override
    public void schedule(String topic, String tag, Runnable task, long intervalMillis) {
        log.debug("[delay-core][XxlJobDelayTaskScheduler] registered normal task and that topic={},tag={}", topic, tag);
        // 注册 dispatch
        String jobName = buildJobName(topic, tag);
        taskMap.put(jobName, task);
        taskIntervalMap.put(jobName, intervalMillis);
    }

    @Override
    public void retrySchedule(String topic, String tag, Runnable task, long intervalMillis) {
        log.debug("[delay-core][ScheduledExecutorScheduler] register retry executor started first time and that topic={},tag={}", topic, tag);
        // 注册 retry dispatch
        String jobName = buildRetryJobName(topic, tag);
        taskMap.put(jobName, task);
        taskIntervalMap.put(jobName, intervalMillis);
    }

    /**
     * 统一 JobHandler：通过 jobName 区分任务
     * <>使用方式</>
     * String topic = XxlJobHelper.getJobParam();
     * String tag = XxlJobHelper.getJobParam();
     * XxlJobDelayTaskScheduler.executeJob(topic，tag);
     *
     * @param topic
     * @param tag
     */
    @Override
    public void executeJob(String topic, String tag) {
        String jobName = buildJobName(topic, tag);
        Runnable task = taskMap.get(jobName);
        if (task != null) {
            task.run();
        } else {
            log.warn("[delay-core]xxl-job no task found for: {}", jobName);
        }
    }

    @Override
    public void executeRetryJob(String topic, String tag) {
        String jobName = buildRetryJobName(topic, tag);
        Runnable task = taskMap.get(jobName);
        if (task != null) {
            task.run();
        } else {
            log.warn("[delay-core]xxl-job no retry task found for: {}", jobName);
        }
    }

    private String buildJobName(String topic, String tag) {
        return "delay_dispatch_" + topic + "_" + tag;
    }

    private String buildRetryJobName(String topic, String tag) {
        return "delay_retry_" + topic + "_" + tag;
    }

}

