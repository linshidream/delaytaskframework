package com.linshidream.delaytaskcore;


import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;

/**
 * Created on 2025/4/9 13:34
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 负责实际的任务处理逻辑
 */

public interface DelayTaskHandler<T extends BaseDelayMqMessage> {

    /**
     * 处理任务
     *
     * @param task 原任务
     * @return
     */
    DelayTaskResult<T> handleTask(T task);
}
