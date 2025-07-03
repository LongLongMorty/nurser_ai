package com.nursing.center.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.util.Random;

/**
 * 字符串工具类
 */
@Slf4j
public class StringUtils {

    private static final Random random = new Random();

    /**
     * 判断字符串是否为空或只包含空白字符
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空且不只包含空白字符
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 安全的字符串trim
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 字符串首字母大写
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 字符串首字母小写
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }

    /**
     * 生成随机数字字符串
     */
    public static String generateRandomNumbers(int length) {
        String numbers = "0123456789";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        return result.toString();
    }

    /**
     * 字符串脱敏处理
     */
    public static String mask(String str, int start, int end, char maskChar) {
        if (isBlank(str) || start < 0 || end > str.length() || start >= end) {
            return str;
        }
        StringBuilder masked = new StringBuilder(str);
        for (int i = start; i < end; i++) {
            masked.setCharAt(i, maskChar);
        }
        return masked.toString();
    }

    /**
     * 字符串中间脱敏
     */
    public static String maskMiddle(String str, int keepStart, int keepEnd) {
        if (isBlank(str) || str.length() <= keepStart + keepEnd) {
            return str;
        }
        return mask(str, keepStart, str.length() - keepEnd, '*');
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToSnake(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }

    /**
     * 下划线转驼峰
     */
    public static String snakeToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size;
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        return nf.format(fileSize) + " " + units[unitIndex];
    }

    /**
     * 生成指定长度的编号（补零）
     */
    public static String generateCode(String prefix, long number, int totalLength) {
        String numberStr = String.valueOf(number);
        int prefixLength = prefix == null ? 0 : prefix.length();
        int numberLength = totalLength - prefixLength;
        
        if (numberLength <= 0) {
            return prefix + numberStr;
        }
        
        String format = "%0" + numberLength + "d";
        return (prefix == null ? "" : prefix) + String.format(format, number);
    }

    /**
     * 截取字符串，超出部分用省略号表示
     */
    public static String truncate(String str, int maxLength) {
        if (isBlank(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
