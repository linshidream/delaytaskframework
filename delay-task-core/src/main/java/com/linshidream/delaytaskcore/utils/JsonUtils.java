package com.linshidream.delaytaskcore.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhengxing
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 对象转 JSON 字符串
     */
    public static String toJSONString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字符串转复杂对象（带泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("JSON 泛型反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 对象之间深度转换（如 DTO ↔ Entity）
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        return MAPPER.convertValue(source, targetClass);
    }

    /**
     * 对象之间深度转换（带泛型）
     */
    public static <T> T convert(Object source, TypeReference<T> typeRef) {
        return MAPPER.convertValue(source, typeRef);
    }
}
