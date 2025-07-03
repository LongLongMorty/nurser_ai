package com.nursing.center.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器工具
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 为admin123生成正确的BCrypt哈希
        String password = "admin123";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hashedPassword);
        
        // 验证生成的哈希是否正确
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("验证结果: " + matches);
    }
}
