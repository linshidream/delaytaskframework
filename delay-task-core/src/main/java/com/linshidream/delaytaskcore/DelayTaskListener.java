package com.linshidream.delaytaskcore;

import java.lang.annotation.*;

/**
 * Created on 2025/4/9 17:23
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DelayTaskListener {

    /**
     * 延迟任务的 topic 名称
     */
    String topic() default "";

    /**
     * 必填：监听的 tag
     */
    String tag();

    /**
     * 轮询间隔时间（默认 60 秒）
     */
    long pollingIntervalSeconds() default 10;

    /**
     * 单次轮询最大条数
     *
     * @return
     */
    int pollTasksMax() default 64;

    /**
     * @return
     */
    String routeStrategy() default "LAST";
}
