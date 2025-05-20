package com.linshidream.delaytaskcore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 2025/4/9 13:44
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
public class RedisTaskStorage implements TaskStorage, Serializable {

    private final RedissonClient redissonClient;

    public RedisTaskStorage(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildKey(String topic, String tag) {
        return "delay:" + topic + ":" + tag;
    }

    private String buildRetryKey(String topic, String tag) {
        return "delay%RETRY%:" + topic + ":" + tag;
    }

    @Override
    public <T extends BaseDelayMqMessage> SaveTaskResult addTask(String topic, String tag, T task) {
        String key = buildKey(topic, tag);
        long score = task.getExecuteTime();

        try {
            String value = objectMapper.writeValueAsString(task);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
            zset.add(score, value);
            return SaveTaskResult.success(task.getTraceId());
        } catch (Exception e) {
            log.error("[delay-core]RedisTaskStorage#addTask catch exception,", e);
            return SaveTaskResult.failure(task.getTraceId(), "RedisTaskStorage Add task fail, " + e.getMessage());
        }
    }

    @Override
    public <T extends BaseDelayMqMessage> List<T> pollExpiredTasks(String topic, String tag, long maxTimestamp, int maxCount, Class<T> clazz) {
        String key = buildKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);

        Collection<String> raws = zset.valueRange(0, true, maxTimestamp, true, 0, maxCount);
        List<T> result = new ArrayList<>();

        for (String raw : raws) {
            try {
                T msg = objectMapper.readValue(raw, clazz);
                result.add(msg);
            } catch (Exception e) {
                log.error("[delay-core]RedisTaskStorage#pollExpiredTasks catch exception,", e);
            }
        }

        return result;
    }

    @Override
    public <T extends BaseDelayMqMessage> void onTaskExecuted(String topic, String tag, List<DelayTaskResult<T>> delayTaskResults) {
        String delayKey = buildKey(topic, tag);

        List<String> tasks = delayTaskResults.stream()
                .map(result -> {
                    try {
                        return objectMapper.writeValueAsString(result.getTask());
                    } catch (Exception e) {
                        log.error("[delay-core]RedisTaskStorage#onTaskExecuted Task serialized fail，taskid={},", result.getTask().getTraceId(), e);
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(delayKey);
        boolean removed = zset.removeAll(tasks);
        log.info("[delay-core]The task callback successed,and that has removed，topic={}, tag={}, removed={}", topic, tag, removed);
    }


    @Override
    public <T extends BaseDelayMqMessage> SaveTaskResult addRetryTask(String topic, String tag, T task) {

        String key = buildRetryKey(topic, tag);
        long score = task.getExecuteTime();
        try {
            String value = objectMapper.writeValueAsString(task);
            RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);
            zset.add(score, value);
            return SaveTaskResult.success(task.getTraceId());
        } catch (Exception e) {
            log.error("[delay-core]RedisTaskStorage#addRetryTask catch exception,", e);
            return SaveTaskResult.failure(task.getTraceId(), "RedisTaskStorage add retry tasks fail," + e.getMessage());
        }
    }

    @Override
    public <T extends BaseDelayMqMessage> List<T> pollExpiredRetryTasks(String topic, String tag, long maxTimestamp, int maxCount, Class<T> clazz) {
        String key = buildRetryKey(topic, tag);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(key);

        Collection<String> raws = zset.valueRange(0, true, maxTimestamp, true, 0, maxCount);
        List<T> result = new ArrayList<>();

        for (String raw : raws) {
            try {
                T msg = objectMapper.readValue(raw, clazz);
                result.add(msg);
            } catch (Exception e) {
                log.error("[delay-core]RedisTaskStorage#pollExpiredRetryTasks catch exception,", e);
            }
        }
        return result;
    }

    @Override
    public <T extends BaseDelayMqMessage> void onRetryTaskExecuted(String topic, String tag, List<DelayTaskResult<T>> delayTaskResults) {
        String delayKey = buildRetryKey(topic, tag);

        List<String> tasks = delayTaskResults.stream()
                .map(result -> {
                    try {
                        return objectMapper.writeValueAsString(result.getTask());
                    } catch (Exception e) {
                        log.error("[delay-core]RedisTaskStorage#onRetryTaskExecuted The retry task serialized fail，taskid={},", result.getTask().getTraceId(), e);
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());

        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(delayKey);
        boolean removed = zset.removeAll(tasks);
        log.info("[delay-core]The retry task callback successed,and that has removed，topic={}, tag={}, removed={}", topic, tag, removed);
    }

}
