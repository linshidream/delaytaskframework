package com.linshidream.delaytaskcore.config;

import com.linshidream.delaytaskcore.*;
import com.linshidream.delaytaskcore.distributor.DelayTaskInstanceRegistry;
import com.linshidream.delaytaskcore.distributor.RedisDelayTaskInstanceRegistry;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2025/4/10 10:36
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Configuration
@ComponentScan("com.linshidream.delaytaskcore")
public class DelayTaskConfiguration {

    //如果有多个实现  @ConditionalOnProperty(name = "delay.task.storage", havingValue = "REDIS")

    /**
     * 延迟任务框架-死信任务存储(是否要定期删除消息-可配置化)
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DeadTaskStorage.class)
    public DeadTaskStorage deadTaskStorage(RedissonClient redissonClient) {
        return new RedisDeadTaskStorage(redissonClient, Boolean.FALSE);
    }

    /**
     * 延迟任务框架-普通任务存储
     *
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(TaskStorage.class)
    public TaskStorage redisStorage(RedissonClient redissonClient) {
        return new RedisTaskStorage(redissonClient);
    }

    /**
     * 延迟任务框架-调度器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DelayTaskScheduler.class)
    public DelayTaskScheduler scheduledExecutorScheduler() {
        return new ScheduledExecutorScheduler();
    }

    /**
     * 延迟任务框架-任务处理器注册中心
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DelayTaskInstanceRegistry.class)
    public DelayTaskInstanceRegistry delayTaskInstanceRegistry(RedissonClient redissonClient) {
        return new RedisDelayTaskInstanceRegistry(redissonClient);
    }

}
