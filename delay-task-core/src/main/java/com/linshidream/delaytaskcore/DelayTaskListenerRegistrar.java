package com.linshidream.delaytaskcore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created on 2025/4/9 17:24
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Slf4j
@Component
public class DelayTaskListenerRegistrar implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Resource
    private DelayTaskDispatcher delayTaskDispatcher;

    @Resource
    private DelayTaskRetryDispatcher delayTaskRetryDispatcher;

    @Resource
    private DelayTaskHandlerRegistry delayTaskHandlerRegistry;

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(DelayTaskListener.class);

        // 注册执行器
        for (Object bean : beans.values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            DelayTaskListener listener = beanClass.getAnnotation(DelayTaskListener.class);

            Type[] interfaces = beanClass.getGenericInterfaces();
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    if (pt.getRawType().equals(DelayTaskHandler.class)) {
                        Class<?> payloadClass = (Class<?>) pt.getActualTypeArguments()[0];
                        delayTaskHandlerRegistry.registerHandler((DelayTaskHandler<?>) bean, payloadClass, listener);
                    }
                }
            }
        }

        // 注册时间轮
        for (Object bean : beans.values()) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            DelayTaskListener listener = beanClass.getAnnotation(DelayTaskListener.class);
            String topic = listener.topic();
            String tag = listener.tag();
            String routeStrategy = listener.routeStrategy();
            long interval = listener.pollingIntervalSeconds() * 1000;
            delayTaskDispatcher.startSchedule(topic, tag, routeStrategy, interval);
            delayTaskRetryDispatcher.startSchedule(topic, tag, routeStrategy, interval);
        }

        log.debug("[delay-core][DelayTaskListenerRegistrar] DelayTaskListener startup success!");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
