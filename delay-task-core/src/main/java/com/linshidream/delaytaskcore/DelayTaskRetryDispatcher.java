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
public class DelayTaskRetryDispatcher {

    @Resource
    private DelayTaskScheduler delayTaskScheduler;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    @Resource
    private TaskStorage taskStorage;

    @Resource
    private DelayTaskDispatcher delayTaskDispatcher;

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
        delayTaskScheduler.retrySchedule(topic, tag, () -> retryPollAndDispatch(topic, tag, routeStrategy), interval);
    }


    /**
     * 获取到期任务并分发执行
     */
    @SuppressWarnings("unchecked")
    private <T extends BaseDelayMqMessage> void retryPollAndDispatch(String topic, String tag, String routeStrategy) {
        try {
            if (!delayTaskStrategyRouter.shouldSchedule(topic, tag, routeStrategy)) {
                log.debug("[delay-core][DelayTaskRetryDispatcher] The current client should not dispatch, topic={},tag={},routeStrategy:{}", topic, tag, routeStrategy);
                return;
            }
            log.debug("[delay-core][DelayTaskRetryDispatcher] The new turn is starting ,poll and dispatch topic={},tag={}", topic, tag);
            Class<T> clazz = (Class<T>) delayTaskHandlerRegistry.getHandlerClazz(topic, tag);
            DelayTaskHandler<T> delayTaskHandler = (DelayTaskHandler<T>) delayTaskHandlerRegistry.getHandlerBean(topic, tag);
            int maxCount = delayTaskHandlerRegistry.getHandlerMaxCount(topic, tag);
            long maxTimestamp = System.currentTimeMillis();
            List<T> tasks = taskStorage.pollExpiredRetryTasks(topic, tag, maxTimestamp, maxCount, clazz);
            if (tasks == null || tasks.size() == 0) {
                return;
            }
            log.info("[delay-core][DelayTaskRetryDispatcher] Has polled retry tasks,the total count={},topic={},tag={},count", tasks.size(), topic, tag);
            List<DelayTaskResult<T>> delayTaskResults = new ArrayList<>();
            for (T task : tasks) {
                log.info("[delay-core][DelayTaskRetryDispatcher] retry task={}", JsonUtils.toJSONString(task));
                DelayTaskResult<T> delayTaskResult = delayTaskDispatcher.dispatchTask(delayTaskHandler, task);
                log.info("[delay-core][DelayTaskRetryDispatcher] retry task has dispatched result={}", JsonUtils.toJSONString(delayTaskResult));
                if (delayTaskResult == null) {
                    continue;
                }
                delayTaskResults.add(delayTaskResult);
            }
            taskStorage.onRetryTaskExecuted(topic, tag, delayTaskResults);

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
            delayTaskDispatcher.retryStorageTasks(topic, tag, retryDelayTasks);
        } catch (Exception e) {
            log.error("[delay-core][DelayTaskRetryDispatcher] pollAndDispatch execption,", e);
        }
    }
}