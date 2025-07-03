package com.nursing.center;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  11:22
 * @Description: TODO
 * @Version: 1.0
 */


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 东软颐养中心管理系统启动类
 *
 * @author dongsoft
 * @since 2024-01-15
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.nursing.center.mapper")
public class NursingCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(NursingCenterApplication.class, args);
        System.out.println("====================================");
        System.out.println("东软颐养中心管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("====================================");
    }
}
