package com.linshidream.delaytaskcore;

import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created on 2025/4/9 14:58
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
@Component
public class DelayTaskEngine {

    @Resource
    private TaskStorage taskStorage;

    /**
     * 提交任务
     */
    public <T extends BaseDelayMqMessage> SaveTaskResult submit(String topic, String tag, T task) {
        log.info("[delay-core][DelayTaskEngine] Save task started,topic={},tag={},task={}", topic, tag, task);
        SaveTaskResult saveTaskResult = taskStorage.addTask(topic, tag, task);
        log.info("[delay-core][DelayTaskEngine] The task added ,taskid={},result={}", task.getTraceId(), saveTaskResult);
        return saveTaskResult;
    }


    /**
     * 提交任务 走线程池
     */
    @Async
    public <T extends BaseDelayMqMessage> void asyncSubmit(String topic, String tag, T task) {
        log.info("[delay-core][DelayTaskEngine] Save task started,topic={},tag={},task={}", topic, tag, task);
        SaveTaskResult saveTaskResult = taskStorage.addTask(topic, tag, task);
        log.info("[delay-core][DelayTaskEngine] The task added ,taskid={},result={}", task.getTraceId(), saveTaskResult);
    }

}
