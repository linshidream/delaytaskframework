package com.linshidream.delaytaskcore;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * Created on 2025/4/9 14:02
 *
 * @author zhengxing
 * @version 1.0.0
 * @description 异步任务结果
 */
@Data
public class SaveTaskResult implements Serializable {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 任务ID (生成的唯一 ID）
     */
    private String taskId;

    /**
     * 保存时间戳
     */
    private Instant savedAt;

    /**
     * 错误信息（仅失败时存在）
     */
    private String message;

    public static SaveTaskResult success(String taskId) {
        SaveTaskResult result = new SaveTaskResult();
        result.success = true;
        result.taskId = taskId;
        result.savedAt = Instant.now();
        return result;
    }

    public static SaveTaskResult failure(String taskId, String message) {
        SaveTaskResult result = new SaveTaskResult();
        result.success = false;
        result.taskId = taskId;
        result.message = message;
        result.savedAt = Instant.now();
        return result;
    }
}
