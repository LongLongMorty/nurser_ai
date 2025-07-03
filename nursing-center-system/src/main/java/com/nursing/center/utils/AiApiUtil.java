package com.nursing.center.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI API调用工具类
 * 处理与DeepSeek等AI模型的交互
 */
@Component
@Slf4j
public class AiApiUtil {
    
    @Value("${ai.deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    
    @Value("${ai.deepseek.api.key:}")
    private String deepseekApiKey;
    
    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepseekModel;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AiApiUtil() {
        // 配置RestTemplate的超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000); // 连接超时60秒
        factory.setReadTimeout(60000);    // 读取超时60秒
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 调用DeepSeek API
     */
    public AiApiResponse callDeepSeekApi(String prompt) {
        AiApiResponse response = new AiApiResponse();
        response.setRequestTime(LocalDateTime.now());
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                deepseekApiUrl, entity, String.class);
            
            response.setResponseTime(LocalDateTime.now());
            response.setResponseStatus("SUCCESS");
            response.setResponseContent(responseEntity.getBody());
            
            // 解析响应内容
            String responseBody = responseEntity.getBody();
            String content = parseResponseContent(responseBody);
            response.setParsedContent(content);
            
            log.info("DeepSeek API调用成功，响应长度：{}", 
                responseBody != null ? responseBody.length() : 0);
            
        } catch (Exception e) {
            response.setResponseTime(LocalDateTime.now());
            response.setResponseStatus("ERROR");
            response.setErrorMessage(e.getMessage());
            
            log.error("DeepSeek API调用失败", e);
            
            // 返回模拟数据作为降级处理
            response.setParsedContent(generateFallbackResponse(prompt));
        }
        
        // 计算响应时间
        if (response.getRequestTime() != null && response.getResponseTime() != null) {
            long duration = java.time.Duration.between(
                response.getRequestTime(), response.getResponseTime()).toMillis();
            response.setResponseDuration((int) duration);
        }
        
        return response;
    }
    
    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepseekModel);
        requestBody.put("max_tokens", 2000);
        requestBody.put("temperature", 0.7);
        
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", new Object[]{message});
        
        return requestBody;
    }
    
    /**
     * 解析API响应内容
     */
    private String parseResponseContent(String responseBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> firstChoice = ((java.util.List<Map<String, Object>>) 
                responseMap.get("choices")).get(0);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            
            return (String) message.get("content");
            
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            return responseBody; // 返回原始响应
        }
    }
    
    /**
     * 生成降级响应（当API调用失败时）
     */
    private String generateFallbackResponse(String prompt) {
        if (prompt.contains("早餐")) {
            return generateBreakfastFallback();
        } else if (prompt.contains("午餐")) {
            return generateLunchFallback();
        } else if (prompt.contains("晚餐")) {
            return generateDinnerFallback();
        } else if (prompt.contains("加餐")) {
            return generateSnackFallback();
        } else {
            return generateGeneralFallback();
        }
    }
    
    private String generateBreakfastFallback() {
        return "{\n" +
               "  \"breakfast\": {\n" +
               "    \"name\": \"营养小米粥套餐\",\n" +
               "    \"description\": \"根据个人情况定制的营养早餐\",\n" +
               "    \"ingredients\": \"小米50g，鸡蛋1个，青菜100g，胡萝卜丝50g\",\n" +
               "    \"cookingMethod\": \"1.小米洗净煮粥30分钟 2.鸡蛋蒸制成蛋羹 3.青菜焯水调味\",\n" +
               "    \"calories\": 350,\n" +
               "    \"nutrition\": \"热量约350千卡，蛋白质15g，富含维生素B1和优质蛋白\"\n" +
               "  },\n" +
               "  \"lunch\": {\n" +
               "    \"name\": \"健康午餐\",\n" +
               "    \"description\": \"营养均衡的午餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 450,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"dinner\": {\n" +
               "    \"name\": \"清淡晚餐\",\n" +
               "    \"description\": \"易消化的晚餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 380,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"snack\": {\n" +
               "    \"name\": \"健康加餐\",\n" +
               "    \"description\": \"营养补充加餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 120,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"totalCalories\": 1300,\n" +
               "  \"recommendationReason\": \"易消化吸收，营养均衡，适合老年人餐饮需求\"\n" +
               "}";
    }
    
    private String generateLunchFallback() {
        return "{\n" +
               "  \"breakfast\": {\n" +
               "    \"name\": \"营养早餐\",\n" +
               "    \"description\": \"健康营养的早餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 350,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"lunch\": {\n" +
               "    \"name\": \"清蒸鲈鱼套餐\",\n" +
               "    \"description\": \"优质蛋白质丰富的午餐\",\n" +
               "    \"ingredients\": \"鲈鱼150g，米饭100g，时令蔬菜150g，紫菜蛋花汤\",\n" +
               "    \"cookingMethod\": \"1.鲈鱼清蒸8分钟 2.蔬菜清炒 3.紫菜汤调味\",\n" +
               "    \"calories\": 450,\n" +
               "    \"nutrition\": \"热量约450千卡，蛋白质25g，低脂高蛋白\"\n" +
               "  },\n" +
               "  \"dinner\": {\n" +
               "    \"name\": \"清淡晚餐\",\n" +
               "    \"description\": \"易消化的晚餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 380,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"snack\": {\n" +
               "    \"name\": \"健康加餐\",\n" +
               "    \"description\": \"营养补充加餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 120,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"totalCalories\": 1300,\n" +
               "  \"recommendationReason\": \"优质蛋白丰富，低脂肪，营养价值高\"\n" +
               "}";
    }
    
    private String generateDinnerFallback() {
        return "{\n" +
               "  \"breakfast\": {\n" +
               "    \"name\": \"营养早餐\",\n" +
               "    \"description\": \"健康营养的早餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 350,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"lunch\": {\n" +
               "    \"name\": \"健康午餐\",\n" +
               "    \"description\": \"营养均衡的午餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 450,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"dinner\": {\n" +
               "    \"name\": \"莲藕排骨汤\",\n" +
               "    \"description\": \"滋补养血的晚餐汤品\",\n" +
               "    \"ingredients\": \"排骨100g，莲藕200g，胡萝卜50g，米饭80g\",\n" +
               "    \"cookingMethod\": \"1.排骨焯水 2.与莲藕炖煮1小时 3.调味即可\",\n" +
               "    \"calories\": 380,\n" +
               "    \"nutrition\": \"热量约380千卡，蛋白质18g，富含胶原蛋白\"\n" +
               "  },\n" +
               "  \"snack\": {\n" +
               "    \"name\": \"健康加餐\",\n" +
               "    \"description\": \"营养补充加餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 120,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"totalCalories\": 1300,\n" +
               "  \"recommendationReason\": \"滋补养血，易消化，适合晚餐\"\n" +
               "}";
    }
    
    private String generateSnackFallback() {
        return "{\n" +
               "  \"breakfast\": {\n" +
               "    \"name\": \"营养早餐\",\n" +
               "    \"description\": \"健康营养的早餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 350,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"lunch\": {\n" +
               "    \"name\": \"健康午餐\",\n" +
               "    \"description\": \"营养均衡的午餐搭配\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 450,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"dinner\": {\n" +
               "    \"name\": \"清淡晚餐\",\n" +
               "    \"description\": \"易消化的晚餐\",\n" +
               "    \"ingredients\": \"优质食材搭配\",\n" +
               "    \"cookingMethod\": \"健康烹饪方式\",\n" +
               "    \"calories\": 380,\n" +
               "    \"nutrition\": \"营养均衡\"\n" +
               "  },\n" +
               "  \"snack\": {\n" +
               "    \"name\": \"银耳莲子羹\",\n" +
               "    \"description\": \"润燥安神的营养加餐\",\n" +
               "    \"ingredients\": \"银耳20g，莲子30g，红枣5颗，冰糖适量\",\n" +
               "    \"cookingMethod\": \"1.银耳泡发 2.与莲子红枣同煮40分钟 3.加冰糖调味\",\n" +
               "    \"calories\": 120,\n" +
               "    \"nutrition\": \"热量约120千卡，富含胶质和维生素\"\n" +
               "  },\n" +
               "  \"totalCalories\": 1300,\n" +
               "  \"recommendationReason\": \"润燥安神，营养丰富，适合加餐\"\n" +
               "}";
    }
    
    private String generateGeneralFallback() {
        return "{\n" +
               "  \"breakfast\": {\n" +
               "    \"name\": \"营养小米粥套餐\",\n" +
               "    \"description\": \"根据个人情况定制的营养早餐\",\n" +
               "    \"ingredients\": \"小米50g，鸡蛋1个，青菜100g，胡萝卜丝50g\",\n" +
               "    \"cookingMethod\": \"1.小米洗净煮粥30分钟 2.鸡蛋蒸制成蛋羹 3.青菜焯水调味\",\n" +
               "    \"calories\": 350,\n" +
               "    \"nutrition\": \"热量约350千卡，蛋白质15g，富含维生素B1和优质蛋白\"\n" +
               "  },\n" +
               "  \"lunch\": {\n" +
               "    \"name\": \"清蒸鲈鱼套餐\",\n" +
               "    \"description\": \"优质蛋白质丰富的午餐\",\n" +
               "    \"ingredients\": \"鲈鱼150g，米饭100g，时令蔬菜150g，紫菜蛋花汤\",\n" +
               "    \"cookingMethod\": \"1.鲈鱼清蒸8分钟 2.蔬菜清炒 3.紫菜汤调味\",\n" +
               "    \"calories\": 450,\n" +
               "    \"nutrition\": \"热量约450千卡，蛋白质25g，低脂高蛋白\"\n" +
               "  },\n" +
               "  \"dinner\": {\n" +
               "    \"name\": \"莲藕排骨汤\",\n" +
               "    \"description\": \"滋补养血的晚餐汤品\",\n" +
               "    \"ingredients\": \"排骨100g，莲藕200g，胡萝卜50g，米饭80g\",\n" +
               "    \"cookingMethod\": \"1.排骨焯水 2.与莲藕炖煮1小时 3.调味即可\",\n" +
               "    \"calories\": 380,\n" +
               "    \"nutrition\": \"热量约380千卡，蛋白质18g，富含胶原蛋白\"\n" +
               "  },\n" +
               "  \"snack\": {\n" +
               "    \"name\": \"银耳莲子羹\",\n" +
               "    \"description\": \"润燥安神的营养加餐\",\n" +
               "    \"ingredients\": \"银耳20g，莲子30g，红枣5颗，冰糖适量\",\n" +
               "    \"cookingMethod\": \"1.银耳泡发 2.与莲子红枣同煮40分钟 3.加冰糖调味\",\n" +
               "    \"calories\": 120,\n" +
               "    \"nutrition\": \"热量约120千卡，富含胶质和维生素\"\n" +
               "  },\n" +
               "  \"totalCalories\": 1300,\n" +
               "  \"recommendationReason\": \"符合膳食指南，营养均衡，满足老年人营养需求\"\n" +
               "}";
    }
    
    /**
     * AI API响应封装类
     */
    public static class AiApiResponse {
        private LocalDateTime requestTime;
        private LocalDateTime responseTime;
        private Integer responseDuration;
        private String responseStatus;
        private String responseContent;
        private String parsedContent;
        private String errorMessage;
        private Integer tokenUsage;
        
        // Getters and Setters
        public LocalDateTime getRequestTime() { return requestTime; }
        public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
        
        public LocalDateTime getResponseTime() { return responseTime; }
        public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
        
        public Integer getResponseDuration() { return responseDuration; }
        public void setResponseDuration(Integer responseDuration) { this.responseDuration = responseDuration; }
        
        public String getResponseStatus() { return responseStatus; }
        public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }
        
        public String getResponseContent() { return responseContent; }
        public void setResponseContent(String responseContent) { this.responseContent = responseContent; }
        
        public String getParsedContent() { return parsedContent; }
        public void setParsedContent(String parsedContent) { this.parsedContent = parsedContent; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Integer getTokenUsage() { return tokenUsage; }
        public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }
    }
}
