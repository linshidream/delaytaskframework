package com.linshidream.delaytaskcore.distributor.impl;

import com.linshidream.delaytaskcore.distributor.DelayTaskAssignStrategy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created on 2025/4/15 10:02
 *
 * @author zhengxing
 * @version 1.0.0
 * @description <p>设计思路<p/>
 * 构建一个虚拟节点的哈希环（例如 64 个虚拟节点）
 * 对 topic+tag 进行 hash，然后在环上顺时针找最近的节点
 * 选中该虚拟节点背后的真实实例
 * 确保多个实例中只有一个负责该 tag 的调度
 */

@Service("CONSISTENT_HASH")
public class ConsistentHashStrategy implements DelayTaskAssignStrategy {

    private static final int VIRTUAL_NODE_NUM = 64;

    @Override
    public String strategy() {
        return "CONSISTENT_HASH";
    }

    @Override
    public boolean shouldSchedule(String topic, String tag, String instanceId, List<String> healthyInstances) {
        // 构建 hash 环
        TreeMap<Integer, String> hashRing = new TreeMap<>();
        for (String inst : healthyInstances) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = hash(inst + "#" + i);
                hashRing.put(hash, inst);
            }
        }

        // 对 topic+tag 做 hash，找到负责节点
        int tagHash = hash(topic + tag);
        SortedMap<Integer, String> tailMap = hashRing.tailMap(tagHash);
        String responsible = tailMap.isEmpty() ? hashRing.firstEntry().getValue() : tailMap.get(tailMap.firstKey());

        return instanceId.equals(responsible);
    }

    public static int hash(String key) {
        final byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        int h = 0x9747b28c;
        for (byte b : bytes) {
            h ^= b;
            h *= 0x5bd1e995;
            h ^= h >> 15;
        }
        return Math.abs(h);
    }
}
