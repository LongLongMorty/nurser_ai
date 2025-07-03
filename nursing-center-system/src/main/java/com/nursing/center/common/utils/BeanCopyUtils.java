package com.nursing.center.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean转换工具类
 */
@Slf4j
public class BeanCopyUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 单个对象拷贝
     */
    public static <T> T copyBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Bean拷贝失败", e);
            return null;
        }
    }

    /**
     * 列表对象拷贝
     */
    public static <T> List<T> copyBeanList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target = copyBean(source, targetClass);
            if (target != null) {
                targetList.add(target);
            }
        }
        return targetList;
    }

    /**
     * 通过JSON转换（深拷贝）
     */
    public static <T> T copyBeanByJson(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(source);
            return objectMapper.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }

    /**
     * 列表通过JSON转换（深拷贝）
     */
    public static <T> List<T> copyBeanListByJson(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String json = objectMapper.writeValueAsString(sourceList);
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, targetClass));
        } catch (JsonProcessingException e) {
            log.error("JSON列表转换失败", e);
            return Collections.emptyList();
        }
    }
}
