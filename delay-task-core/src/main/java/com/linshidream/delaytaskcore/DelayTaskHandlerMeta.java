package com.linshidream.delaytaskcore;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2025/4/11 16:25
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 处理器元数据
 */

@Data
public class DelayTaskHandlerMeta implements Serializable {

    /**
     * 唯一值
     */
    private String destination;

    /**
     * topic
     */
    private String topic;

    /**
     * tag
     */
    private String tag;

    /**
     * 路由策略
     */
    private String routeStrategy;


    /**
     * 轮询间隔时间（默认 60 秒）
     */
    private Long pollingIntervalSeconds;

    /**
     * 单次轮询最大条数
     *
     * @return
     */
    private Integer pollTasksMax;


    /**
     * 任务目标类
     */
    private Class<?> taskClazz;

    /**
     * 处理器目标类
     */
    private Class<?> handlerClazz;
}
