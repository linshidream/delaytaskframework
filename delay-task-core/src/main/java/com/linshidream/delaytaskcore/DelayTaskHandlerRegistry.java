package com.linshidream.delaytaskcore;

import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2025/4/10 15:04
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
@Component
public class DelayTaskHandlerRegistry {


    private final Map<String, DelayTaskHandler<?>> handlerRegistry = new ConcurrentHashMap<>();

    private final Map<String, Class<?>> payloadClassRegistry = new ConcurrentHashMap<>();

    private final Map<String, Integer> maxCountRegistry = new ConcurrentHashMap<>();

    private final List<DelayTaskHandlerMeta> delayTaskHandlerMetas = new ArrayList<>();

    /**
     * 注册处理器
     *
     * @param handler
     */
    public <T extends BaseDelayMqMessage> void registerHandler(DelayTaskHandler<?> handler, Class<?> clazz, DelayTaskListener listener) {
        String topic = listener.topic();
        String tag = listener.tag();
        String strategy = listener.routeStrategy();

        String dest = buildDestination(topic, tag);
        handlerRegistry.put(dest, handler);
        payloadClassRegistry.put(dest, clazz);
        maxCountRegistry.put(dest, listener.pollTasksMax());

        DelayTaskHandlerMeta meta = new DelayTaskHandlerMeta();
        meta.setDestination(dest);
        meta.setTopic(topic);
        meta.setTag(tag);
        meta.setRouteStrategy(strategy);
        meta.setPollTasksMax(listener.pollTasksMax());
        meta.setPollingIntervalSeconds(listener.pollingIntervalSeconds());
        meta.setTaskClazz(clazz);
        meta.setHandlerClazz(handler.getClass());
        delayTaskHandlerMetas.add(meta);

        log.debug("[delay-core]HandlerRegistry register one handler that topic={},tag={},clazz={}", topic, tag, clazz);
    }

    public DelayTaskHandler<?> getHandlerBean(String topic, String tag) {
        DelayTaskHandler<?> handler = handlerRegistry.get(buildDestination(topic, tag));
        if (handler == null) {
            log.warn("[delay-core]HandlerRegistry cant find the delayTaskHandler that topic={},tag={} ，skipped", topic, tag);
            return null;
        }
        return handler;
    }

    public Class<?> getHandlerClazz(String topic, String tag) {
        Class<?> clazz = payloadClassRegistry.get(buildDestination(topic, tag));
        if (clazz == null) {
            log.warn("[delay-core]HandlerRegistry cant find the handlerClazz that topic={},tag={} ，skipped", topic, tag);
            return null;
        }
        return clazz;
    }

    public Integer getHandlerMaxCount(String topic, String tag) {
        return maxCountRegistry.getOrDefault(buildDestination(topic, tag), 64);
    }


    public List<DelayTaskHandlerMeta> getAllHanderMetas() {
        return delayTaskHandlerMetas;
    }

    public String buildDestination(String topic, String tag) {
        return topic + "-" + tag;
    }

    public String[] express(String destination) {
        return destination.split("-");
    }

}
