package com.nursing.center.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 验证工具类
 */
@Slf4j
public class ValidationUtils {

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 身份证号正则表达式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    /**
     * 验证手机号
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches() && validateIdCardChecksum(idCard);
    }

    /**
     * 验证邮箱
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证字符串是否为空或只包含空白字符
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证字符串是否不为空且不只包含空白字符
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 验证年龄范围
     */
    public static boolean isValidAge(Integer age) {
        return age != null && age >= 0 && age <= 150;
    }

    /**
     * 验证血型
     */
    public static boolean isValidBloodType(String bloodType) {
        return bloodType != null && 
               (bloodType.equals("A") || bloodType.equals("B") || 
                bloodType.equals("AB") || bloodType.equals("O"));
    }

    /**
     * 验证身份证校验位
     */
    private static boolean validateIdCardChecksum(String idCard) {
        try {
            int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
            char[] checksums = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
            
            int sum = 0;
            for (int i = 0; i < 17; i++) {
                sum += Character.getNumericValue(idCard.charAt(i)) * weights[i];
            }
            
            char expectedChecksum = checksums[sum % 11];
            char actualChecksum = Character.toUpperCase(idCard.charAt(17));
            
            return expectedChecksum == actualChecksum;
        } catch (Exception e) {
            log.error("身份证校验位验证失败", e);
            return false;
        }
    }

    /**
     * 从身份证号提取出生日期
     */
    public static String extractBirthDateFromIdCard(String idCard) {
        if (!isValidIdCard(idCard)) {
            return null;
        }
        String year = idCard.substring(6, 10);
        String month = idCard.substring(10, 12);
        String day = idCard.substring(12, 14);
        return year + "-" + month + "-" + day;
    }

    /**
     * 从身份证号提取性别
     */
    public static String extractGenderFromIdCard(String idCard) {
        if (!isValidIdCard(idCard)) {
            return null;
        }
        int genderCode = Character.getNumericValue(idCard.charAt(16));
        return genderCode % 2 == 0 ? "女" : "男";
    }

    /**
     * 验证密码强度（至少8位，包含字母和数字）
     */
    public static boolean isValidPassword(String password) {
        if (isBlank(password) || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }

    /**
     * 手机号脱敏显示
     */
    public static String maskPhone(String phone) {
        if (!isValidPhone(phone)) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏显示
     */
    public static String maskIdCard(String idCard) {
        if (!isValidIdCard(idCard)) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
}
