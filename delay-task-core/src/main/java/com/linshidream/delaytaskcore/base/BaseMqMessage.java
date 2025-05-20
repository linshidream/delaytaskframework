package com.linshidream.delaytaskcore.base;

import lombok.Data;

import java.util.Date;
import java.util.UUID;


/**
 * @author zhengxing
 */
@Data
public abstract class BaseMqMessage {
    /**
     * 业务键
     */
    protected String key;
    /**
     * 消息来源
     */
    protected String source = "";
    /**
     * 发送时间
     */
    protected Date sendTime = new Date();
    /**
     * 跟踪id业务链
     */
    protected String traceId = UUID.randomUUID().toString();
    /**
     * 重试次数，用于判断重试次数，超过重试次数发送异常警告
     */
    protected Integer retryTimes = 0;
    /**
     * 非必要,顺序发送
     */
    protected String hashKey;
}
