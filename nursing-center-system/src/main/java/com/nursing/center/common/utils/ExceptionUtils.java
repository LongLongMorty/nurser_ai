package com.nursing.center.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类
 */
@Slf4j
public class ExceptionUtils {

    /**
     * 获取异常的详细堆栈信息
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * 获取异常的根本原因
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * 获取异常的根本原因消息
     */
    public static String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        return rootCause != null ? rootCause.getMessage() : "";
    }

    /**
     * 获取简化的异常信息（类名 + 消息）
     */
    public static String getSimpleMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    /**
     * 安全执行代码块，捕获异常并记录日志
     */
    public static void safeExecute(Runnable runnable, String operationName) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error("执行{}时发生异常", operationName, e);
        }
    }

    /**
     * 安全执行代码块，捕获异常并返回默认值
     */
    public static <T> T safeExecute(java.util.function.Supplier<T> supplier, T defaultValue, String operationName) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("执行{}时发生异常，返回默认值", operationName, e);
            return defaultValue;
        }
    }
}
