package com.linshidream.delaytaskcore.distributor;

import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2025/4/14 15:44
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 维护实例ID
 */

public class InstanceIdGenerator {

    private static volatile String cachedInstanceId;

    /**
     * 获取本机实例ID，[本地IP]@[注册中心节点IP:端口]@[PID or 时间戳]
     *
     * @return
     */
    public static String generate(String registryAddr) {
        if (cachedInstanceId != null) {
            return cachedInstanceId;
        }

        synchronized (InstanceIdGenerator.class) {
            if (cachedInstanceId == null) {
                String localIp = getLocalIp();
                long pid = getProcessId();
                long random = ThreadLocalRandom.current().nextLong(10000, 99999);
                cachedInstanceId = String.format("%s@%s@%d%d", localIp, registryAddr, pid, random);
            }
        }
        return cachedInstanceId;
    }

    private static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static long getProcessId() {
        try {
            // JDK 9+
            Class<?> handleClass = Class.forName("java.lang.ProcessHandle");
            Object current = handleClass.getMethod("current").invoke(null);
            return (long) handleClass.getMethod("pid").invoke(current);
        } catch (Throwable ignore) {
            // fallback for Java 8
            String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            try {
                return Long.parseLong(jvmName.split("@")[0]);
            } catch (Exception e) {
                return System.currentTimeMillis();
            }
        }
    }
}
