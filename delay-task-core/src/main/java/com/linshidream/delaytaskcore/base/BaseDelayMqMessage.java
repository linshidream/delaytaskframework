package com.linshidream.delaytaskcore.base;

import com.linshidream.delaytaskcore.constant.DelayLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;


/**
 * @author zhengxing
 */
@Data
public abstract class BaseDelayMqMessage extends BaseMqMessage {
    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 10;

    /**
     * 延时等级 【RocketMQ 专用】
     */
    private Integer delayLevel = DelayLevel.FIVE_SECOND;

    /**
     * 最大延时等级 【RocketMQ 专用】
     */
    private Integer maxDelayLevel = DelayLevel.TWO_HOUR;

    /**
     * 延时时间【delay-task 专用】
     */
    private Long executeTime;

    public Long toMilli(LocalDateTime data) {
        return data.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
