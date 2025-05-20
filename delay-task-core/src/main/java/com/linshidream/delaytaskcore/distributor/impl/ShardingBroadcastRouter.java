package com.linshidream.delaytaskcore.distributor.impl;


import com.linshidream.delaytaskcore.distributor.DelayTaskAssignStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created on 2025/4/15 10:20
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

@Service("SHARDING_BROADCAST")
public class ShardingBroadcastRouter implements DelayTaskAssignStrategy {

    @Override
    public String strategy() {
        return "SHARDING_BROADCAST";
    }

    @Override
    public boolean shouldSchedule(String topic, String tag, String instanceId, List<String> healthyInstances) {
        // 分片执行 每个分片都可以执行
        return true;
    }
}
