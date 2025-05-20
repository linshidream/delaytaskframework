package com.linshidream.delaytaskcore;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/4/9 17:56
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
public class ScheduledExecutorScheduler implements DelayTaskScheduler {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int NORMAL_POOL_SIZE = CPU_CORES * 2;
    private static final int RETRY_POOL_SIZE = (int) Math.ceil(CPU_CORES * 1.5);

    /**
     * 普通线程池
     */
    private final ScheduledExecutorService normalExecutor = new ScheduledThreadPoolExecutor(
            NORMAL_POOL_SIZE,
            r -> {
                Thread t = new Thread(r);
                t.setName("delay-core-normal-" + t.getId());
                t.setDaemon(true);
                return t;
            }
    );

    /**
     * 重试任务线程池
     */
    private final ScheduledExecutorService retryExecutor = new ScheduledThreadPoolExecutor(
            RETRY_POOL_SIZE,
            r -> {
                Thread t = new Thread(r);
                t.setName("delay-core-retry-" + t.getId());
                t.setDaemon(true);
                return t;
            }
    );

    @Override
    public void schedule(String topic, String tag, Runnable task, long intervalMillis) {
        // 错峰启动：随机初始延迟 5~60 秒
        log.debug("[delay-core][ScheduledExecutorScheduler] register normal executor started first time and that topic={},tag={}", topic, tag);
        long initialDelayMillis = 5 + new Random().nextInt(56) * 1000;
        normalExecutor.scheduleAtFixedRate(task, initialDelayMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void retrySchedule(String topic, String tag, Runnable task, long intervalMillis) {
        log.debug("[delay-core][ScheduledExecutorScheduler] register retry executor started first time and that topic={},tag={}", topic, tag);
        // 错峰启动：随机初始延迟 5~60 秒
        long initialDelayMillis = 5 + new Random().nextInt(56) * 1000;
        retryExecutor.scheduleAtFixedRate(task, initialDelayMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel(String topic, String tag) {
        normalExecutor.shutdownNow();
        retryExecutor.shutdownNow();
        log.warn("[delay-core][ScheduledExecutorScheduler] NormalExecutor and retryExecutor both shutdowned! and that topic={},tag={}", topic, tag);
    }
}

