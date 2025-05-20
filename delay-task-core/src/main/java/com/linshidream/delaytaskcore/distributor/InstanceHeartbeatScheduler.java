package com.linshidream.delaytaskcore.distributor;

import com.linshidream.delaytaskcore.DelayTaskHandlerMeta;
import com.linshidream.delaytaskcore.DelayTaskHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/4/15 11:42
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 负责节点注册和健康检查
 */
@Slf4j
@Component
public class InstanceHeartbeatScheduler {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private DelayTaskInstanceRegistry delayTaskInstanceRegistry;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    private final ScheduledExecutorService heartbeatScheduler = new ScheduledThreadPoolExecutor(
            2,
            r -> {
                Thread t = new Thread(r);
                t.setName("delay-core-heartbeat-" + t.getId());
                t.setDaemon(true);
                return t;
            }
    );


    @PostConstruct
    public void start() {
        // 注册实例
        final List<DelayTaskHandlerMeta> allHanderMetas = delayTaskHandlerRegistry.getAllHanderMetas();

        final String instanceId = InstanceIdGenerator.generate(delayTaskInstanceRegistry.getRegistryAddr());

        try {
            this.registerInstance();
        } catch (Exception e) {
            log.error("[delay-core]Register instance failed again!", e);
        }

        // 定时心跳 每10秒心跳一次
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            for (DelayTaskHandlerMeta handlerMeta : allHanderMetas) {
                String topic = handlerMeta.getTopic();
                String tag = handlerMeta.getTag();
                try {
                    delayTaskInstanceRegistry.instanceHeartbeat(topic, tag, instanceId);

                    log.debug("[delay-core]️ Heartbeat renewed for topic={},tag={},instanceId={}", topic, tag, instanceId);
                } catch (Exception e) {
                    log.warn("[delay-core]️ Heartbeat renew failed for topic={},tag={},instanceId={}", topic, tag, instanceId, e);
                }
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        final List<DelayTaskHandlerMeta> allHanderMetas = delayTaskHandlerRegistry.getAllHanderMetas();
        final String instanceId = InstanceIdGenerator.generate(delayTaskInstanceRegistry.getRegistryAddr());
        heartbeatScheduler.shutdownNow();
        for (DelayTaskHandlerMeta allHanderMeta : allHanderMetas) {
            String topic = allHanderMeta.getTopic();
            String tag = allHanderMeta.getTag();
            delayTaskInstanceRegistry.unregisterInstance(topic, tag, instanceId);
        }
    }

    private void registerInstance() {
        final List<DelayTaskHandlerMeta> allHanderMetas = delayTaskHandlerRegistry.getAllHanderMetas();
        final String instanceId = InstanceIdGenerator.generate(delayTaskInstanceRegistry.getRegistryAddr());
        for (DelayTaskHandlerMeta allHanderMeta : allHanderMetas) {
            String topic = allHanderMeta.getTopic();
            String tag = allHanderMeta.getTag();
            try {
                delayTaskInstanceRegistry.registerInstance(topic, tag, instanceId);
                log.info("[delay-core]Register instance success for topic={},tag={},instanceId={}", topic, tag, instanceId);
            } catch (Exception e) {
                log.error("[delay-core]Register instance failed for topic={},tag={},instanceId={}", topic, tag, instanceId, e);
                // 注册失败重试
                delayTaskInstanceRegistry.registerInstance(topic, tag, instanceId);
            }
        }
    }

}
