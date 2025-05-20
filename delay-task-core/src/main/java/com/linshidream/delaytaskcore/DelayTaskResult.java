package com.linshidream.delaytaskcore;

import com.linshidream.delaytaskcore.base.BaseDelayMqMessage;
import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/4/9 14:34
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */
@Data
public class DelayTaskResult<T extends BaseDelayMqMessage> implements Serializable {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 是否需要重试（false 表示不再重试，即便失败）
     */
    private Boolean needRetry;

    /**
     * 处理结果，用于日志或告警
     */
    private String message;

    /**
     * 原任务，用于重新投递并重试
     */
    private T task;

    public static <T extends BaseDelayMqMessage> DelayTaskResult<T> success(String message, T task) {
        return new DelayTaskResult(true, false, message, task);
    }

    public static <T extends BaseDelayMqMessage> DelayTaskResult<T> failureAndRetry(String message, T task) {
        return new DelayTaskResult(false, true, message, task);
    }

    public static <T extends BaseDelayMqMessage> DelayTaskResult<T> failureAndNonRetry(String message, T task) {
        return new DelayTaskResult(false, false, message, task);
    }

    public DelayTaskResult(boolean success, boolean needRetry, String message, T task) {
        this.success = success;
        this.needRetry = needRetry;
        this.message = message;
        this.task = task;
    }

}
