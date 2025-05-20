package com.linshidream.delaytaskadmin.web;

import com.linshidream.delaytaskcore.DelayTaskEngine;
import com.linshidream.delaytaskcore.DelayTaskHandlerRegistry;
import com.linshidream.delaytaskcore.DelayTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created on 2025/4/11 16:50
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */
@Slf4j
@RestController
@RequestMapping("/DelayTaskTest")
public class DelayTaskTestController {

    @Resource
    private DelayTaskEngine delayTaskEngine;

    @Resource
    private DelayTaskScheduler delayTaskScheduler;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    @GetMapping("/metas")
    public Object getAllHanderMetas() {
        return delayTaskHandlerRegistry.getAllHanderMetas();
    }
    @GetMapping("/xxl")
    public void xxlJob(String topic, String tag) {
        delayTaskScheduler.executeJob(topic, tag);
    }

    @GetMapping("/xxltry")
    public void xxltry(String topic, String tag) {
        delayTaskScheduler.executeRetryJob(topic, tag);
    }
}
