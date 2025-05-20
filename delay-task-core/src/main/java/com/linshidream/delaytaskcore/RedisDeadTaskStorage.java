package com.linshidream.delaytaskcore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2025/4/11 19:20
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
public class RedisDeadTaskStorage implements DeadTaskStorage {

    private final Boolean isAutoCleanup;

    /**
     * 默认清理7天前的数据
     */
    private final Integer cleanupDayLen = 7;

    private final RedissonClient redissonClient;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisDeadTaskStorage(RedissonClient redissonClient, Boolean isAutoCleanup) {
        this.redissonClient = redissonClient;
        this.isAutoCleanup = isAutoCleanup;
    }

    private String buildDlKey(String topic, String tag) {
        return "delay%DLQ%:" + topic + ":" + tag;
    }

    @Override
    public <T extends BaseDelayMqMessage> void save(String topic, String tag, List<T> taskList) {
        String key = buildDlKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
        taskList.forEach(task -> {
            try {
                String json = objectMapper.writeValueAsString(task);
                zset.add((double) task.getExecuteTime(), json);
            } catch (Exception e) {
                log.warn("[delay-core][RedisDeadTaskStorage#save] Failed to serialize dead task= {}", task, e);
            }
        });
    }

    @Override
    public List<String> query(String topic, String tag, long startTimeMillis, long endTimeMillis) {
        String key = buildDlKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
        return new ArrayList<>(zset.valueRange((double) startTimeMillis, true, (double) endTimeMillis, true));
    }

    @Override
    public void remove(String topic, String tag, List<String> messageBodies) {
        String key = buildDlKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
        zset.removeAll(messageBodies);
    }

    @Override
    public void clearBefore(String topic, String tag, long beforeTimestamp) {
        String key = buildDlKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
        int removed = zset.removeRangeByScore(0, true, (double) beforeTimestamp, true);
        log.info("[delay-core]Cleared {} dead tasks from key: {} before {}", removed, key, beforeTimestamp);
    }

    @Override
    public void scheduledCleanup() {
        if (!this.isAutoCleanup) {
            log.info("[delay-core]Scheduled dead task cleanup is disabled.");
            return;
        }
        // 默认时间
        long ttl = LocalDateTime.now()
                .minusDays(cleanupDayLen)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        List<DelayTaskHandlerMeta> handerMetas = delayTaskHandlerRegistry.getAllHanderMetas();
        handerMetas.forEach(meta -> clearBefore(meta.getTopic(), meta.getTag(), ttl));
    }
}
