## 📖 README



# Delay Task Framework

一个轻量级、可扩展、支持多种调度模式的分布式延迟任务调度框架，支持任务存储、失败重试、死信处理、主题订阅、错峰调度、分布式路由调度等特性，适用于订单取消、定时通知、自动收货、回调重试等延迟场景。



## ✨ 核心特性

- ✅ 支持延迟任务存储（Redis已实现、MySQL暂未实现）
- ✅ 支持任务处理器动态注册（基于注解自动化加载）
- ✅ 支持任务失败重试（指数退避 + 时钟抖动 避免任务雪崩）
- ✅ 支持死信任务处理（清理、批量处理到最大重试次数的任务）
- ✅ 每个 topic + tag 独立时间轮询 & 共享线程池
- ✅ 支持调度器插件（内置 ScheduledExecutorScheduler、支持接入 XXL-JOB）
- ✅ 支持分布式调度能力 (Redis 注册中心实现，节点注册 + 心跳续期 + 健康实例获取)
- ✅ 支持 LAST、一致性哈希、分片等路由策略 (解决多节点并发调度问题)




## 💡 使用场景

- 订单未支付10分钟后取消
- 注册后1小时发送欢迎邮件
- 已完成订单7天自动收货
- 订单充值结果通知下游系统


## 📝 分布式调度

多节点实例部署环境，使用本地线程池调度器会存在多实例并发调度的情况。可以通过注册中心，根据路由策略来匹配具体的实例执行调度。

- LAST：最后一个注册实例调度（默认方式）

- CONSISTENT_HASH：（计算 topic + tag 的哈希值）一致性哈希分配

- SHARDING_BROADCAST：所有节点同时调度（广播）

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](./LICENSE) file for details.

## 🚀 快速使用

### 1. 引入 maven 依赖

需要注意：引入该 maven 依赖后，只需要注入 RedissonClient 对象，参考 RedissonConfig 配置文件。无需重复引入 Redisson 相关依赖。(存储对列任务依赖redis)
```xml
<dependency>
    <groupId>com.linshidream</groupId>
    <artifactId>delay-task-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 定义任务类

```java
public class OrderNotifyTask extends BaseDelayMqMessage {

    private String orderId;
   // getter/setter
}
```

### 3. 编写任务处理器

```java
@Slf4j
@Component
@DelayTaskListener(topic = "order", tag = "notify", pollingIntervalSeconds = 30, pollTasksMax = 100)
public class OrderNotifyHandler implements DelayTaskHandler<OrderNotifyTask> {
    // 该处理器订阅的主题=order，标签=notify，每30秒轮询一次，每次处理100条

    @Override
    public DelayTaskResult<OrderNotifyTask> handleTask(OrderNotifyTask task) {
      
        try {
            String msg = notify(task.getOrderId());
            return DelayTaskResult.success(msg, task);

        } catch (Exception e) {
            log.error("", e);
            return DelayTaskResult.failureAndRetry(e.getMessage(), task);
        }
    }

    public String notify(String orderId) {
        log.info("模拟延迟任务执行，通知订单返回成功");
        return "ok";
    }
}
```

### 4. 提交任务

```java
public class TestUnit {
    public static void main(String[] args) {
        // 自动注入
        DelayTaskEngine delayTaskEngine = new DelayTaskEngine();
        
        OrderNotifyTask task = new OrderNotifyTask();
        task.setKey(orderId);
        task.setSource("测试订单");
        // 5秒后触发
        task.setExecuteTime(task.toMilli(LocalDateTime.now().plusSeconds(5)));
        task.setOrderId(IdUtil.fastSimpleUUID());
        
        delayTaskEngine.submit("order", "notify", task);
    }
}
```


## 📁 项目结构图

```
delay-task-framework/
├── core/
│   ├── DelayTaskHandler.java              // 延迟任务处理器接口
│   ├── DelayTaskHandlerMeta.java          // 延迟任务处理器元数据
│   ├── DelayTaskResult.java               // 任务执行结果封装
│   ├── DelayTaskListener.java             // 标注监听延迟任务的注解
│   ├── DelayTaskListenerRegistrar.java    // 自动扫描 @DelayTaskListener 注解并注册
│   ├── DelayTaskHandlerRegistry.java      // 任务处理器注册中心
│   ├── DelayTaskEngine.java               // 延迟任务引擎：任务提交&执行入口
│   ├── DelayTaskDispatcher.java           // 任务调度分发器：分发任务到执行器
│   ├── DelayTaskRetryDispatcher.java      // 重试任务调度器：支持退避机制
│   ├── DelayTaskScheduler.java            // 统一调度器：封装线程池 & 调度逻辑
│   ├── ScheduledExecutorScheduler.java    // 本地线程池调度器实现
│   ├── XxlJobDelayTaskScheduler.java      // XXL-Job等外部调度器实现
│   ├── BackoffUtils.java                  // 退避算法工具类（指数退避 + Jitter）
│   ├── DelayTaskConfiguration.java        // Spring Boot 自动配置类
│   ├── TaskStorage.java                   // 延迟任务存储接口
│   ├── RedisDeadTaskStorage.java          // 延迟任务存储 Redis 实现
│   ├── DeadTaskStorage.java               // 延迟死信任务存储接口
│   ├── RedisDeadTaskStorage.java          // 延迟死信任务存储 Redis 实现
├── distributor/
│   ├── ...                                // 分布式调度和注册中心实现
```

------





## 🧩 各文件职责说明

| 类名                         | 描述                                                         |
| ---------------------------- | ------------------------------------------------------------ |
| `DelayTaskHandler`           | 延迟任务处理接口，所有任务处理器需实现此接口。               |
| `DelayTaskHandlerMeta`       | 延迟任务处理器元数据，包含处理器原始信息。               |
| `DelayTaskResult`            | 任务处理结果封装，含是否成功、错误原因、是否重试等。         |
| `DelayTaskListener`          | 标注在处理类上，用于注册为某个 tag/topic 的处理器。          |
| `DelayTaskListenerRegistrar` | 扫描并注册带有 `@DelayTaskListener` 注解的任务处理器。       |
| `DelayTaskHandlerRegistry`   | 注册中心，按 topic+tag 注册处理器，并支持类型泛型绑定。      |
| `DelayTaskEngine`            | 核心引擎：提供任务提交、任务调度的统一入口。                 |
| `DelayTaskDispatcher`        | 任务调度执行器，周期性从存储中拉取任务并交给执行线程池处理。 |
| `DelayTaskRetryDispatcher`   | 处理失败任务的重试逻辑，使用独立线程池 & 支持退避策略。      |
| `DelayTaskScheduler`         | 封装线程池和调度操作，支持错峰调度、动态 tag 轮询等。        |
| `ScheduledExecutorScheduler` | 本地线程池调度器实现。        |
| `XxlJobDelayTaskScheduler`   | XXL-Job等外部调度器实现。        |
| `BackoffUtils`               | 提供指数退避（delay * 2^n）、Jitter、最大重试次数等算法。    |
| `DelayTaskConfiguration`     | Spring Boot 自动装配类，统一初始化调度组件。                 |