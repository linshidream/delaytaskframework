package com.linshidream.delaytaskcore.distributor;


import lombok.Getter;

/**
 * Created on 2025/4/16 17:06
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

public enum TaskRouteStrategy {

    LAST("LAST", "最后一个注册实例调度"),

    CONSISTENT_HASH("CONSISTENT_HASH", "一致性哈希分配"),

    SHARDING_BROADCAST("SHARDING_BROADCAST", "广播调度"),

    ;


    @Getter
    private String strategy;
    @Getter
    private String name;


    TaskRouteStrategy(String strategy, String name) {
        this.strategy = strategy;
        this.name = name;
    }

}
