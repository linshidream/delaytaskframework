package com.linshidream.delaytaskcore;


import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2025/4/10 11:08
 *
 * @author zhengxing
 * @version 1.0.0
 * @description
 */

public class BackoffUtils {

    /**
     * 计算下一次重试时间（时间戳）
     *
     * @param retryCount      当前重试次数
     * @param baseDelayMillis 基础延迟（毫秒），如 1000 表示 1 秒
     * @param maxRetryCount   最大重试次数，超过不再重试
     * @param maxDelayMillis  最大允许延迟，避免无限增长
     * @return 返回下一次应执行的时间（System.currentTimeMillis() + delay）
     */
    public static long computeNextRetryTime(int retryCount, long baseDelayMillis, int maxRetryCount, long maxDelayMillis) {
        if (retryCount >= maxRetryCount) {
            // 表示不再重试
            return -1L;
        }

        // 计算指数退避：baseDelay * 2^retryCount
        long exponentialDelay = baseDelayMillis * (1L << retryCount);

        // 加入 Jitter：±20% 浮动
        long jitterBound = (long) (exponentialDelay * 0.2);
        long jitter = ThreadLocalRandom.current().nextLong(-jitterBound, jitterBound + 1);

        long finalDelay = Math.min(exponentialDelay + jitter, maxDelayMillis);
        return System.currentTimeMillis() + Math.max(finalDelay, baseDelayMillis);
    }

    /**
     * 简化版本，使用默认参数
     */
    public static long computeNextRetryTime(int retryCount, int maxRetryCount) {
        // 默认：初始 1 秒，最大延迟 10 分钟
        return computeNextRetryTime(retryCount, 1000L, maxRetryCount, 10 * 60 * 1000L);
    }
}
