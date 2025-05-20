package com.linshidream.delaytaskcore.distributor.impl;

import com.linshidream.delaytaskcore.distributor.DelayTaskAssignStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created on 2025/4/15 10:02
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */
@Service("LAST")
public class LastInstanceStrategy implements DelayTaskAssignStrategy {

    @Override
    public String strategy() {
        return "LAST";
    }


    @Override
    public boolean shouldSchedule(String topic, String tag, String instanceId, List<String> healthyInstances) {
        // healthyInstances：默认按注册时间正序
        String lastInstances = healthyInstances.get(healthyInstances.size() - 1);
        return instanceId.equals(lastInstances);
    }
}
