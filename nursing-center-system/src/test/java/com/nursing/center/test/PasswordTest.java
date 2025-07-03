package com.nursing.center.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码：admin123
        String plainPassword = "admin123";
        String hashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdQMkhskqfLaOCzf3xCl5HgFzn2";
        
        System.out.println("原始密码: " + plainPassword);
        System.out.println("数据库中的哈希密码: " + hashedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("密码匹配结果: " + matches);
        
        // 重新生成哈希密码测试
        String newHash = encoder.encode(plainPassword);
        System.out.println("新生成的哈希密码: " + newHash);
        
        boolean newMatches = encoder.matches(plainPassword, newHash);
        System.out.println("新哈希密码匹配结果: " + newMatches);
    }
}
