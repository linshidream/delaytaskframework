package com.linshidream.delaytaskcore;

import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import com.linshidream.delaytaskcore.distributor.DelayTaskStrategyRouter;
import com.linshidream.delaytaskcore.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 2025/4/9 15:00
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
@Component
public class DelayTaskDispatcher {

    @Resource
    private TaskStorage taskStorage;

    @Resource
    private DeadTaskStorage deadTaskStorage;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    @Resource
    private DelayTaskScheduler delayTaskScheduler;

    @Resource
    private DelayTaskStrategyRouter delayTaskStrategyRouter;

    /**
     * 注册时间轮
     *
     * @param topic
     * @param tag
     * @param interval
     * @param routeStrategy
     */
    public <T extends BaseDelayMqMessage> void startSchedule(String topic, String tag, String routeStrategy, long interval) {
        delayTaskScheduler.schedule(topic, tag, () -> pollAndDispatch(topic, tag, routeStrategy), interval);
    }


    /**
     * 获取到期任务并分发执行
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseDelayMqMessage> void pollAndDispatch(String topic, String tag, String routeStrategy) {
        try {
            if (!delayTaskStrategyRouter.shouldSchedule(topic, tag, routeStrategy)) {
                log.debug("[delay-core][DelayTaskDispatcher] The current client should not dispatch, topic={},tag={},routeStrategy:{}", topic, tag, routeStrategy);
                return;
            }
            log.debug("[delay-core][DelayTaskDispatcher] The new turn is starting ,poll and dispatch topic={},tag={}", topic, tag);
            Class<T> clazz = (Class<T>) delayTaskHandlerRegistry.getHandlerClazz(topic, tag);
            DelayTaskHandler<T> delayTaskHandler = (DelayTaskHandler<T>) delayTaskHandlerRegistry.getHandlerBean(topic, tag);
            Integer maxCount = delayTaskHandlerRegistry.getHandlerMaxCount(topic, tag);

            // 获取当前时间之前的的数据
            long maxTimestamp = System.currentTimeMillis();
            List<T> tasks = taskStorage.pollExpiredTasks(topic, tag, maxTimestamp, maxCount, clazz);
            if (tasks == null || tasks.size() == 0) {
                return;
            }
            log.info("[delay-core][DelayTaskDispatcher] Has polled tasks,the total count={},topic={},tag={},count", tasks.size(), topic, tag);

            List<DelayTaskResult<T>> delayTaskResults = new ArrayList<>();
            for (T task : tasks) {
                log.info("[delay-core][DelayTaskDispatcher] task={}", JsonUtils.toJSONString(task));
                DelayTaskResult<T> delayTaskResult = dispatchTask(delayTaskHandler, task);
                log.info("[delay-core][DelayTaskDispatcher] task has dispatched result={}", JsonUtils.toJSONString(delayTaskResult));
                if (delayTaskResult == null) {
                    continue;
                }
                delayTaskResults.add(delayTaskResult);
            }

            taskStorage.onTaskExecuted(topic, tag, delayTaskResults);

            // 异常需要重试的再次入队
            List<T> retryDelayTasks = delayTaskResults.stream()
                    .filter(result -> !result.getSuccess())
                    .filter(DelayTaskResult::getNeedRetry)
                    .map(DelayTaskResult::getTask)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (retryDelayTasks.size() == 0) {
                return;
            }
            log.debug("[delay-core][DelayTaskDispatcher] Ops！task dispatch fail, retry tasks={}", JsonUtils.toJSONString(retryDelayTasks));

            this.retryStorageTasks(topic, tag, retryDelayTasks);
        } catch (Exception e) {
            log.error("[delay-core][DelayTaskDispatcher] pollAndDispatch execption,", e);
        }
    }

    /**
     * 调用 handler 执行并回调
     */
    public <T extends BaseDelayMqMessage> DelayTaskResult<T> dispatchTask(DelayTaskHandler<T> handler, T task) {
        try {
            return handler.handleTask(task);
        } catch (Exception e) {
            log.error("[delay-core]DelayTaskDispatcher#dispatchTask catch exception,", e);
            return DelayTaskResult.failureAndNonRetry("Handler exception: " + e.getMessage(), task);
        }
    }


    /**
     * 再次入队
     *
     * @param retryDelayTasks
     * @param <T>
     */
    public <T extends BaseDelayMqMessage> void retryStorageTasks(String topic, String tag, List<T> retryDelayTasks) {
        List<T> dlTask = new ArrayList<>();
        for (T retryTask : retryDelayTasks) {
            long ttl = BackoffUtils.computeNextRetryTime(retryTask.getRetryCount(), retryTask.getMaxRetryCount());
            if (ttl == -1L) {
                log.warn("[delay-core][DelayTaskDispatcher] The task current retry times has overed that max retry times,task={}", retryTask);
                retryTask.setExecuteTime(System.currentTimeMillis());
                dlTask.add(retryTask);
                continue;
            }
            retryTask.setRetryCount(retryTask.getRetryCount() + 1);
            retryTask.setExecuteTime(ttl);
            SaveTaskResult saveTaskResult = taskStorage.addRetryTask(topic, tag, retryTask);
            log.info("[delay-core][DelayTaskDispatcher] The retry task storageed one more times,result={}", saveTaskResult);
        }

        if (dlTask.size() > 0) {
            try {
                // 保存到死信对列存储，score 为当前时间
                deadTaskStorage.save(topic, tag, dlTask);
            } catch (Exception e) {
                log.error("[delay-core][DelayTaskDispatcher] Failed to save dead letter task", e);
            }
        }
    }

}