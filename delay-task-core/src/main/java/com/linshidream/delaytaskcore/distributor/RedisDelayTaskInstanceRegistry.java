package com.linshidream.delaytaskcore.distributor;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/4/14 17:01
 *
 * @author zhengxing
 * @version 1.0.0
 * @description redis 注册中心实现
 *
 * <p>设计方案<p/>
 * 注册顺序表（RList<String>）：
 * key: delay:registry:{topic}:{tag}
 * 内容：实例 ID，保持注册顺序
 * <p>
 * 心跳活跃表（String（带过期））：
 * key: delay:heartbeat:{topic}:{tag}:{instanceId}
 * score：时间戳
 */

@Slf4j
public class RedisDelayTaskInstanceRegistry implements DelayTaskInstanceRegistry {

    private final RedissonClient redissonClient;

    private static final String REGISTER_KEY = "delay:registry:%s:%s";

    private static final String HEARTBEAT_KEY = "delay:heartbeat:%s:%s:%s";
    /**
     * 心跳检查 30秒内健康实例
     */
    private static final long HEARTBEAT_TTL_MILLIS = 30_000;

    public RedisDelayTaskInstanceRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private String buildRegisterKey(String topic, String tag) {
        return String.format(REGISTER_KEY, topic, tag);
    }

    private String buildHeartbeatKey(String topic, String tag, String instanceId) {
        return String.format(HEARTBEAT_KEY, topic, tag, instanceId);
    }

    @Override
    public String getRegistryAddr() {
        // 获取IP和端口
        String address = redissonClient.getConfig().useSingleServer().getAddress();
        if (address.startsWith("redis://")) {
            address = address.substring("redis://".length());
        } else if (address.startsWith("rediss://")) {
            address = address.substring("rediss://".length());
        }
        return address.replaceAll(":", "-");
    }


    @Override
    public void registerInstance(String topic, String tag, String instanceId) {
        // 仅第一次注册时添加
        String regKey = buildRegisterKey(topic, tag);
        RList<String> registerList = redissonClient.getList(regKey);
        registerList.add(instanceId);

        // 立即发送第一次心跳
        instanceHeartbeat(topic, tag, instanceId);
    }


    @Override
    public void unregisterInstance(String topic, String tag, String instanceId) {
        String key = buildRegisterKey(topic, tag);
        RSet<String> set = redissonClient.getSet(key);
        set.remove(instanceId);

        redissonClient.getBucket(buildHeartbeatKey(topic, tag, instanceId)).delete();
    }


    @Override
    public void instanceHeartbeat(String topic, String tag, String instanceId) {
        // 每次续期更新
        try {
            heartbeat(topic, tag, instanceId);
        } catch (Exception e) {
            log.error("[delay-core]Heart beat error,", e);
            // 发生异常后重试
            heartbeat(topic, tag, instanceId);
        }
    }

    public void heartbeat(String topic, String tag, String instanceId) {
        String heartbeatKey = buildHeartbeatKey(topic, tag, instanceId);
        RBucket<String> heartbeat = redissonClient.getBucket(heartbeatKey);
        heartbeat.set("1", HEARTBEAT_TTL_MILLIS, TimeUnit.MILLISECONDS);

        RList<String> instanceList = redissonClient.getList(buildRegisterKey(topic, tag));
        if (!instanceList.contains(instanceId)) {
            log.warn("[delay-core]Instance [{}] lost in registry list for tag [{}], re-registering...", instanceId, tag);
            instanceList.add(instanceId);
        }
    }

    @Override
    public List<String> getOrderedHealthyInstancesAndCleanup(String topic, String tag) {
        RList<String> instanceList = redissonClient.getList(buildRegisterKey(topic, tag));

        List<String> result = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();

        for (String instanceId : instanceList.readAll()) {
            RBucket<String> heartbeat = redissonClient.getBucket(buildHeartbeatKey(topic, tag, instanceId));
            if (heartbeat.isExists()) {
                result.add(instanceId);
            } else {
                toRemove.add(instanceId);
            }
        }

        if (!toRemove.isEmpty()) {
            instanceList.removeAll(toRemove);
        }

        return result;
    }
}


