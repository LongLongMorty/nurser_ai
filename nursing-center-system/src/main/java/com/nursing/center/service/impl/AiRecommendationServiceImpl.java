package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nursing.center.entity.*;
import com.nursing.center.mapper.*;
import com.nursing.center.service.AiRecommendationService;
import com.nursing.center.utils.AgeGenderRecommendationUtil;
import com.nursing.center.utils.AiApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AI推荐服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService {
    
    private final AiRecommendationTempMapper tempMapper;
    private final AiRecommendationHistoryMapper historyMapper;
    private final AiPersonalizedMenuMapper menuMapper;
    private final AiCallLogMapper callLogMapper;
    private final AiApiUtil aiApiUtil;
    
    @Override
    public List<AiRecommendationTemp> generateRecommendations(String mealType, String nutritionType, 
                                                             String targetGroup, String additionalRequirements, 
                                                             Integer[] caloriesRange, Long userId) {
        log.info("生成AI推荐：餐食类型={}, 营养类型={}, 目标群体={}, 热量范围={}", 
                mealType, nutritionType, targetGroup, caloriesRange);
        
        String sessionId = generateSessionId();
        
        // 构建推荐提示词
        String prompt = buildGeneralPrompt(mealType, nutritionType, targetGroup, additionalRequirements, caloriesRange);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        logApiCall(sessionId, "GENERAL", prompt, apiResponse, userId, null);
        
        // 解析AI响应并生成推荐列表
        List<AiRecommendationTemp> recommendations = parseAiResponseToRecommendations(
            apiResponse.getParsedContent(), sessionId, mealType, nutritionType, targetGroup, userId);
        
        // 批量保存推荐
        if (!recommendations.isEmpty()) {
            tempMapper.batchInsert(recommendations);
            // 重新查询保存后的数据（包含ID）
            recommendations = tempMapper.selectBySessionId(sessionId);
        }
        
        return recommendations;
    }
    
    @Override
    public List<AiRecommendationTemp> getCurrentRecommendations(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // 如果没有指定会话ID，返回最近的推荐
            LambdaQueryWrapper<AiRecommendationTemp> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(AiRecommendationTemp::getCreateTime)
                   .last("LIMIT 10");
            return tempMapper.selectList(wrapper);
        }
        
        List<AiRecommendationTemp> recommendations = tempMapper.selectBySessionId(sessionId);
        
        // 如果没有推荐，尝试从历史记录生成
        if (recommendations.isEmpty()) {
            log.info("当前会话无推荐，尝试从历史记录生成");
            recommendations = generateFromHistory("BREAKFAST", "BALANCED", "ELDERLY", 1L);
        }
        
        return recommendations;
    }
    
    @Override
    @Transactional
    public Long adoptRecommendation(Long tempId, Long userId) {
        log.info("采纳推荐，推荐ID={}, 用户ID={}", tempId, userId);
        
        // 获取推荐内容
        AiRecommendationTemp temp = tempMapper.selectById(tempId);
        if (temp == null) {
            throw new RuntimeException("推荐不存在：" + tempId);
        }
        
        // 转换为MealConfig并保存
        // MealConfig mealConfig = convertToMealConfig(temp);
        // TODO: 调用mealConfigService.save(mealConfig)保存
        Long mealConfigId = 1L; // 临时返回固定ID
        
        // 记录采纳历史
        AiRecommendationHistory history = new AiRecommendationHistory();
        history.setMealConfigId(mealConfigId);
        history.setOriginalAiContent(temp.getAiReason());
        history.setMealName(temp.getMealName());
        history.setMealType(temp.getMealType());
        history.setTargetGroup(temp.getTargetGroup());
        history.setNutritionType(temp.getNutritionType());
        history.setAiScore(temp.getAiScore());
        history.setAdoptionDate(LocalDateTime.now());
        history.setAdoptedByUserId(userId);
        history.setIsRecommendedAgain(1);
        
        historyMapper.insert(history);
        
        log.info("推荐采纳成功，生成MealConfig ID={}", mealConfigId);
        return mealConfigId;
    }
    
    @Override
    public List<Long> batchAdoptRecommendations(List<Long> tempIds, Long userId) {
        List<Long> mealConfigIds = new ArrayList<>();
        
        for (Long tempId : tempIds) {
            try {
                Long mealConfigId = adoptRecommendation(tempId, userId);
                mealConfigIds.add(mealConfigId);
            } catch (Exception e) {
                log.error("批量采纳失败，推荐ID={}", tempId, e);
            }
        }
        
        return mealConfigIds;
    }
    
    @Override
    public void clearCurrentRecommendations(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            tempMapper.deleteBySessionId(sessionId);
            log.info("清空会话推荐，会话ID={}", sessionId);
        } else {
            // 清空所有临时推荐
            LambdaQueryWrapper<AiRecommendationTemp> wrapper = new LambdaQueryWrapper<>();
            tempMapper.delete(wrapper);
            log.info("清空所有临时推荐");
        }
    }
    
    @Override
    public boolean hasAdoptionHistory(String mealType, String targetGroup) {
        Long count = historyMapper.countAdoptions(mealType, targetGroup);
        return count != null && count > 0;
    }
    
    @Override
    public List<AiRecommendationTemp> generateFromHistory(String mealType, String nutritionType, 
                                                         String targetGroup, Long userId) {
        log.info("基于历史记录生成推荐");
        
        // 获取高评分的历史记录
        List<AiRecommendationHistory> historyList = historyMapper.selectHighRatedHistory(
            mealType, targetGroup, 7.0, 5);
        
        String sessionId = generateSessionId();
        List<AiRecommendationTemp> recommendations = new ArrayList<>();
        
        for (AiRecommendationHistory history : historyList) {
            AiRecommendationTemp temp = new AiRecommendationTemp();
            temp.setSessionId(sessionId);
            temp.setMealName(history.getMealName());
            temp.setMealType(history.getMealType());
            temp.setTargetGroup(history.getTargetGroup());
            temp.setNutritionType(history.getNutritionType());
            temp.setAiScore(history.getAiScore());
            temp.setAiReason("基于历史高评分推荐");
            temp.setIsFromHistory(1);
            temp.setHistoryRefId(history.getId());
            temp.setUserId(userId);
            temp.setCreateTime(LocalDateTime.now());
            
            recommendations.add(temp);
        }
        
        if (!recommendations.isEmpty()) {
            tempMapper.batchInsert(recommendations);
        }
        
        return recommendations;
    }
    
    @Override
    public List<AiRecommendationHistory> getAdoptionHistory(String mealType, String targetGroup, Integer limit) {
        return historyMapper.selectByConditions(mealType, targetGroup, null, limit);
    }
    
    @Override
    public Long generatePersonalizedMenuWithAgeGender(Long customerId, String customerName, Integer age, String gender,
                                                     LocalDate menuDate, String healthConditions, String dietaryPreferences,
                                                     String allergies, Long userId) {
        log.info("生成个性化菜单，客户ID={}, 年龄={}, 性别={}, 日期={}", customerId, age, gender, menuDate);
        
        // 检查是否已存在该客户该日期的菜单（避免唯一索引冲突）
        AiPersonalizedMenu existingMenu = menuMapper.selectByCustomerAndDate(customerId, menuDate);
        if (existingMenu != null) {
            log.warn("客户 {} 在日期 {} 已存在菜单，返回现有菜单ID: {}", customerId, menuDate, existingMenu.getId());
            return existingMenu.getId();
        }
        
        String sessionId = generateSessionId();
        
        // 构建个性化提示词
        String prompt = AgeGenderRecommendationUtil.generateDailyMenuPrompt(age, gender, healthConditions, dietaryPreferences, allergies);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        logApiCall(sessionId, "PERSONALIZED", prompt, apiResponse, userId, customerId);
        
        // 解析AI响应并生成个性化菜单
        AiPersonalizedMenu menu = parseAiResponseToPersonalizedMenu(
            apiResponse.getParsedContent(), customerId, customerName, age, gender, 
            menuDate, sessionId, healthConditions, dietaryPreferences, allergies, userId);
        
        // 保存菜单
        try {
            menuMapper.insert(menu);
            log.info("个性化菜单生成成功，客户ID={}, 菜单ID={}", customerId, menu.getId());
        } catch (Exception e) {
            log.error("保存个性化菜单失败，可能存在唯一索引冲突", e);
            // 再次检查是否已存在
            existingMenu = menuMapper.selectByCustomerAndDate(customerId, menuDate);
            if (existingMenu != null) {
                return existingMenu.getId();
            }
            throw e;
        }
        
        return menu.getId();
    }
    
    @Override
    public List<AiRecommendationTemp> generatePersonalizedRecommendations(Long customerId, String customerName,
                                                                         Integer age, String gender, String mealType,
                                                                         String nutritionType, String targetGroup,
                                                                         String healthConditions, String dietaryPreferences,
                                                                         String allergies, Long userId) {
        log.info("生成个性化推荐，客户ID={}, 年龄={}, 性别={}", customerId, age, gender);
        
        String sessionId = generateSessionId();
        
        // 构建个性化提示词
        String prompt = AgeGenderRecommendationUtil.generatePersonalizedPrompt(customerName, age, gender, 
            healthConditions, dietaryPreferences, allergies, mealType);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        logApiCall(sessionId, "PERSONALIZED", prompt, apiResponse, userId, customerId);
        
        // 解析AI响应并生成推荐列表
        List<AiRecommendationTemp> recommendations = parseAiResponseToPersonalizedRecommendations(
            apiResponse.getParsedContent(), sessionId, customerId, customerName, age, gender, 
            mealType, nutritionType, targetGroup, userId);
        
        // 批量保存推荐
        if (!recommendations.isEmpty()) {
            tempMapper.batchInsert(recommendations);
            // 重新查询保存后的数据（包含ID）
            recommendations = tempMapper.selectBySessionId(sessionId);
        }
        
        return recommendations;
    }
    
    @Override
    public AiPersonalizedMenu getPersonalizedMenu(Long customerId, LocalDate menuDate) {
        return menuMapper.selectByCustomerAndDate(customerId, menuDate);
    }
    
    @Override
    public List<AiPersonalizedMenu> getPersonalizedMenuHistory(Long customerId, LocalDate startDate, LocalDate endDate) {
        return menuMapper.selectCustomerMenuHistory(customerId, startDate, endDate);
    }
    
    @Override
    public void reviewPersonalizedMenu(Long menuId, Integer isApproved, String reviewComment, Long reviewerId) {
        AiPersonalizedMenu menu = menuMapper.selectById(menuId);
        if (menu != null) {
            menu.setIsApproved(isApproved);
            menu.setNutritionistReview(reviewComment);
            menu.setApprovedBy(reviewerId);
            menu.setApprovedTime(LocalDateTime.now());
            
            menuMapper.updateById(menu);
            log.info("菜单审核完成，菜单ID={}, 审核结果={}", menuId, isApproved);
        }
    }
    
    @Override
    public List<AiRecommendationTemp> generateRecommendationsForCustomer(Long customerId, String mealType,
                                                                         String nutritionType, String targetGroup, Long userId) {
        // TODO: 根据客户ID获取客户信息（年龄、性别等）
        // 这里需要调用CustomerService获取客户详细信息
        
        // 临时使用默认值
        return generatePersonalizedRecommendations(customerId, "客户" + customerId, 70, "FEMALE", 
            mealType, nutritionType, targetGroup, null, null, null, userId);
    }
    
    // ============ 新增方法实现 ============
    
    @Override
    public AiPersonalizedMenu generatePersonalizedMenu(Long customerId, String customerName, Integer age, String gender,
                                                      LocalDate menuDate, String healthConditions, String dietaryPreferences,
                                                      String allergies) {
        Long menuId = generatePersonalizedMenuWithAgeGender(customerId, customerName, age, gender, 
            menuDate, healthConditions, dietaryPreferences, allergies, 1L);
        
        return menuMapper.selectById(menuId);
    }
    
    @Override
    public List<AiPersonalizedMenu> getCustomerPersonalizedMenus(Long customerId, LocalDate startDate, LocalDate endDate) {
        return getPersonalizedMenuHistory(customerId, startDate, endDate);
    }
    
    @Override
    public void approvePersonalizedMenu(Long menuId, String reviewComment) {
        reviewPersonalizedMenu(menuId, 1, reviewComment, 1L);
    }
    
    @Override
    public Map<String, Object> getAiCallStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            Map<String, Object> apiStats = callLogMapper.selectApiStatistics(startDate, endDate);
            Double successRate = callLogMapper.selectSuccessRate(startDate, endDate);
            
            statistics.put("apiStatistics", apiStats);
            statistics.put("successRate", successRate);
            statistics.put("period", Map.of("startDate", startDate, "endDate", endDate));
            
        } catch (Exception e) {
            log.error("获取AI统计失败", e);
            statistics.put("error", "统计数据获取失败");
        }
        
        return statistics;
    }
    
    @Override
    public String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    public String callDeepSeekApi(String prompt) {
        AiApiUtil.AiApiResponse response = aiApiUtil.callDeepSeekApi(prompt);
        return response.getParsedContent();
    }
    
    // ============ 私有辅助方法 ============
    
    private String buildGeneralPrompt(String mealType, String nutritionType, String targetGroup, 
                                     String additionalRequirements, Integer[] caloriesRange) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为").append(getTargetGroupDescription(targetGroup))
              .append("推荐适合的").append(getMealTypeDescription(mealType))
              .append("，营养类型要求：").append(getNutritionTypeDescription(nutritionType));
        
        // 添加热量范围要求
        if (caloriesRange != null && caloriesRange.length >= 2) {
            prompt.append("，热量控制在").append(caloriesRange[0])
                  .append("-").append(caloriesRange[1]).append("千卡之间");
        }
        
        if (additionalRequirements != null && !additionalRequirements.trim().isEmpty()) {
            prompt.append("，附加要求：").append(additionalRequirements);
        }
        
        prompt.append("。\n\n请严格按照以下JSON格式返回3个推荐，每个推荐包含完整信息：\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"mealName\": \"菜品名称\",\n");
        prompt.append("      \"description\": \"菜品描述\",\n");
        prompt.append("      \"mainFood\": \"主食\",\n");
        prompt.append("      \"sideDishes\": \"配菜\",\n");
        prompt.append("      \"soup\": \"汤品（如有）\",\n");
        prompt.append("      \"fruits\": \"水果（如有）\",\n");
        prompt.append("      \"ingredients\": \"详细食材清单\",\n");
        prompt.append("      \"cookingMethod\": \"制作方法步骤\",\n");
        prompt.append("      \"calories\": 热量数值,\n");
        prompt.append("      \"protein\": 蛋白质含量,\n");
        prompt.append("      \"fat\": 脂肪含量,\n");
        prompt.append("      \"carbohydrate\": 碳水化合物含量,\n");
        prompt.append("      \"nutritionInfo\": \"营养分析说明\",\n");
        prompt.append("      \"suitableCrowd\": \"适合人群\",\n");
        prompt.append("      \"estimatedCost\": 预估成本,\n");
        prompt.append("      \"aiScore\": 评分(1-10),\n");
        prompt.append("      \"aiReason\": \"推荐理由\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("注意：\n");
        prompt.append("1. 必须返回有效的JSON格式\n");
        prompt.append("2. 热量、蛋白质等数值字段不要加单位\n");
        prompt.append("3. 确保菜品适合").append(getTargetGroupDescription(targetGroup)).append("\n");
        prompt.append("4. 考虑").append(getNutritionTypeDescription(nutritionType)).append("的要求\n");
        
        // 如果有热量范围，强调热量控制
        if (caloriesRange != null && caloriesRange.length >= 2) {
            prompt.append("5. 严格控制每个菜品热量在").append(caloriesRange[0])
                  .append("-").append(caloriesRange[1]).append("千卡范围内\n");
        }
        
        return prompt.toString();
    }
    
    private void logApiCall(String sessionId, String apiType, String prompt, AiApiUtil.AiApiResponse apiResponse, 
                           Long userId, Long customerId) {
        AiCallLog log = new AiCallLog();
        log.setSessionId(sessionId);
        log.setApiType(apiType);
        log.setRequestParams(prompt);
        log.setAiModel("deepseek");
        log.setRequestTime(apiResponse.getRequestTime());
        log.setResponseTime(apiResponse.getResponseTime());
        log.setResponseDuration(apiResponse.getResponseDuration());
        log.setResponseStatus(apiResponse.getResponseStatus());
        log.setResponseContent(apiResponse.getResponseContent());
        log.setErrorMessage(apiResponse.getErrorMessage());
        log.setUserId(userId);
        log.setCustomerId(customerId);
        log.setCreateTime(LocalDateTime.now());
        
        callLogMapper.insert(log);
    }
    
    private List<AiRecommendationTemp> parseAiResponseToRecommendations(String aiContent, String sessionId, 
                                                                       String mealType, String nutritionType, 
                                                                       String targetGroup, Long userId) {
        List<AiRecommendationTemp> recommendations = new ArrayList<>();
        
        try {
            // 尝试解析JSON格式的AI响应
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiContent);
            JsonNode recommendationsNode = rootNode.get("recommendations");
            
            if (recommendationsNode != null && recommendationsNode.isArray()) {
                for (JsonNode recNode : recommendationsNode) {
                    AiRecommendationTemp temp = new AiRecommendationTemp();
                    temp.setSessionId(sessionId);
                    temp.setMealName(getJsonString(recNode, "mealName", "推荐菜品"));
                    temp.setMealType(mealType);
                    temp.setDescription(getJsonString(recNode, "description", ""));
                    temp.setMainFood(getJsonString(recNode, "mainFood", ""));
                    temp.setSideDishes(getJsonString(recNode, "sideDishes", ""));
                    temp.setSoup(getJsonString(recNode, "soup", ""));
                    temp.setFruits(getJsonString(recNode, "fruits", ""));
                    temp.setIngredients(getJsonString(recNode, "ingredients", ""));
                    temp.setCookingMethod(getJsonString(recNode, "cookingMethod", ""));
                    temp.setCalories(getJsonBigDecimal(recNode, "calories", BigDecimal.ZERO));
                    temp.setProtein(getJsonBigDecimal(recNode, "protein", BigDecimal.ZERO));
                    temp.setFat(getJsonBigDecimal(recNode, "fat", BigDecimal.ZERO));
                    temp.setCarbohydrate(getJsonBigDecimal(recNode, "carbohydrate", BigDecimal.ZERO));
                    temp.setNutritionInfo(getJsonString(recNode, "nutritionInfo", ""));
                    temp.setSuitableCrowd(getJsonString(recNode, "suitableCrowd", ""));
                    temp.setTargetGroup(targetGroup);
                    temp.setNutritionType(nutritionType);
                    temp.setEstimatedCost(getJsonBigDecimal(recNode, "estimatedCost", BigDecimal.ZERO));
                    temp.setAiScore(getJsonBigDecimal(recNode, "aiScore", new BigDecimal("8.0")));
                    temp.setAiReason(getJsonString(recNode, "aiReason", "AI推荐"));
                    temp.setAiModel("deepseek");
                    temp.setIsFromHistory(0);
                    temp.setUserId(userId);
                    temp.setCreateTime(LocalDateTime.now());
                    
                    recommendations.add(temp);
                }
            }
        } catch (Exception e) {
            log.warn("解析AI JSON响应失败，使用智能文本解析方法", e);
            // 智能备用解析：尝试从AI响应文本中提取更多有用信息
            recommendations = parseAiResponseWithSmartTextExtraction(aiContent, sessionId, mealType, nutritionType, targetGroup, userId);
        }
        
        return recommendations;
    }
    
    /**
     * 智能文本解析方法 - 从AI响应中提取更多有用信息
     */
    private List<AiRecommendationTemp> parseAiResponseWithSmartTextExtraction(String aiContent, String sessionId, 
                                                                            String mealType, String nutritionType, 
                                                                            String targetGroup, Long userId) {
        List<AiRecommendationTemp> recommendations = new ArrayList<>();
        
        try {
            // 首先尝试从可能破损的JSON中提取信息
            List<AiRecommendationTemp> jsonExtracted = extractFromPartialJson(aiContent, sessionId, mealType, nutritionType, targetGroup, userId);
            if (!jsonExtracted.isEmpty()) {
                return jsonExtracted;
            }
            
            // 如果JSON提取失败，尝试智能文本分析
            AiRecommendationTemp temp = new AiRecommendationTemp();
            temp.setSessionId(sessionId);
            temp.setMealType(mealType);
            temp.setTargetGroup(targetGroup);
            temp.setNutritionType(nutritionType);
            temp.setAiModel("deepseek");
            temp.setIsFromHistory(0);
            temp.setUserId(userId);
            temp.setCreateTime(LocalDateTime.now());
            
            // 智能提取菜品名称
            String extractedMealName = smartExtractMealName(aiContent);
            temp.setMealName(extractedMealName != null ? extractedMealName : "AI推荐菜品");
            
            // 智能提取描述信息
            String extractedDescription = smartExtractDescription(aiContent);
            temp.setDescription(extractedDescription != null ? extractedDescription : "营养搭配餐食");
            
            // 智能提取食材信息
            String extractedIngredients = smartExtractIngredients(aiContent);
            temp.setIngredients(extractedIngredients != null ? extractedIngredients : "优质食材搭配");
            
            // 智能提取烹饪方法
            String extractedCookingMethod = smartExtractCookingMethod(aiContent);
            temp.setCookingMethod(extractedCookingMethod != null ? extractedCookingMethod : "健康烹饪方式");
            
            // 智能提取营养信息
            String extractedNutrition = smartExtractNutritionInfo(aiContent);
            temp.setNutritionInfo(extractedNutrition != null ? extractedNutrition : "营养均衡");
            
            // 智能提取推荐理由
            String extractedReason = smartExtractReasonInfo(aiContent);
            temp.setAiReason(extractedReason != null ? extractedReason : "AI智能推荐");
            
            // 尝试提取数值信息
            temp.setCalories(smartExtractCalories(aiContent));
            temp.setProtein(smartExtractProtein(aiContent));
            temp.setCarbohydrate(smartExtractCarbohydrate(aiContent));
            temp.setFat(smartExtractFat(aiContent));
            
            // 设置默认评分
            temp.setAiScore(new BigDecimal("8.0"));
            
            recommendations.add(temp);
            
        } catch (Exception e) {
            log.error("智能文本解析也失败，使用最基本的解析", e);
            // 最后的兜底方案
            AiRecommendationTemp fallbackTemp = createFallbackRecommendation(sessionId, mealType, nutritionType, targetGroup, userId);
            recommendations.add(fallbackTemp);
        }
        
        return recommendations;
    }
    
    // JSON解析辅助方法
    private String getJsonString(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : defaultValue;
    }
    
    private BigDecimal getJsonBigDecimal(JsonNode node, String fieldName, BigDecimal defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode != null && !fieldNode.isNull()) {
            try {
                return new BigDecimal(fieldNode.asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private List<AiRecommendationTemp> parseAiResponseToPersonalizedRecommendations(String aiContent, String sessionId, 
                                                                                   Long customerId, String customerName, 
                                                                                   Integer age, String gender, String mealType, 
                                                                                   String nutritionType, String targetGroup, Long userId) {
        List<AiRecommendationTemp> recommendations = new ArrayList<>();
        
        AiRecommendationTemp temp = new AiRecommendationTemp();
        temp.setSessionId(sessionId);
        temp.setCustomerId(customerId);
        temp.setCustomerName(customerName);
        temp.setCustomerAge(age);
        temp.setCustomerGender(gender);
        temp.setMealName(extractMealName(aiContent, mealType != null ? mealType : "推荐", "AI推荐菜品"));
        temp.setMealType(mealType);
        temp.setDescription(aiContent);
        temp.setIngredients(extractIngredients(aiContent, mealType != null ? mealType : "推荐"));
        temp.setCookingMethod(extractCookingMethod(aiContent, mealType != null ? mealType : "推荐"));
        temp.setNutritionInfo(extractNutritionInfo(aiContent, mealType != null ? mealType : "推荐"));
        temp.setTargetGroup(targetGroup);
        temp.setNutritionType(nutritionType);
        temp.setAiScore(new BigDecimal("9.0"));
        temp.setAiReason("基于年龄" + age + "岁、性别" + gender + "的个性化推荐");
        temp.setAiModel("deepseek");
        temp.setIsFromHistory(0);
        temp.setUserId(userId);
        temp.setUserAge(age);
        temp.setUserGender(gender);
        temp.setCreateTime(LocalDateTime.now());
        
        recommendations.add(temp);
        
        return recommendations;
    }
    
    private AiPersonalizedMenu parseAiResponseToPersonalizedMenu(String aiContent, Long customerId, String customerName,
                                                               Integer age, String gender, LocalDate menuDate, String sessionId,
                                                               String healthConditions, String dietaryPreferences, 
                                                               String allergies, Long userId) {
        AiPersonalizedMenu menu = new AiPersonalizedMenu();
        
        // 设置基本信息
        menu.setCustomerId(customerId);
        menu.setCustomerName(customerName);
        menu.setCustomerAge(age);
        menu.setCustomerGender(gender);
        menu.setMenuDate(menuDate);
        menu.setSessionId(sessionId);
        menu.setHealthConditions(healthConditions);
        menu.setDietaryPreferences(dietaryPreferences);
        menu.setAllergies(allergies);
        menu.setAiRawResponse(aiContent);
        menu.setIsApproved(0);
        menu.setCreatedByUserId(userId);
        menu.setCreateTime(LocalDateTime.now());
        
        try {
            // 尝试解析JSON格式的AI响应
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(aiContent);
            
            // 解析早餐信息
            if (jsonNode.has("breakfast")) {
                JsonNode breakfast = jsonNode.get("breakfast");
                menu.setBreakfastName(getJsonStringValue(breakfast, "name", "营养早餐"));
                menu.setBreakfastDescription(getJsonStringValue(breakfast, "description", "根据个人情况定制的营养早餐"));
                menu.setBreakfastIngredients(getJsonStringValue(breakfast, "ingredients", "优质食材搭配"));
                menu.setBreakfastCookingMethod(getJsonStringValue(breakfast, "cookingMethod", "健康烹饪方式"));
                menu.setBreakfastCalories(getJsonDecimalValue(breakfast, "calories", new BigDecimal("350")));
                menu.setBreakfastNutrition(getJsonStringValue(breakfast, "nutrition", "营养均衡"));
            }
            
            // 解析午餐信息
            if (jsonNode.has("lunch")) {
                JsonNode lunch = jsonNode.get("lunch");
                menu.setLunchName(getJsonStringValue(lunch, "name", "健康午餐"));
                menu.setLunchDescription(getJsonStringValue(lunch, "description", "营养均衡的午餐搭配"));
                menu.setLunchIngredients(getJsonStringValue(lunch, "ingredients", "优质食材搭配"));
                menu.setLunchCookingMethod(getJsonStringValue(lunch, "cookingMethod", "健康烹饪方式"));
                menu.setLunchCalories(getJsonDecimalValue(lunch, "calories", new BigDecimal("450")));
                menu.setLunchNutrition(getJsonStringValue(lunch, "nutrition", "营养均衡"));
            }
            
            // 解析晚餐信息
            if (jsonNode.has("dinner")) {
                JsonNode dinner = jsonNode.get("dinner");
                menu.setDinnerName(getJsonStringValue(dinner, "name", "清淡晚餐"));
                menu.setDinnerDescription(getJsonStringValue(dinner, "description", "易消化的晚餐"));
                menu.setDinnerIngredients(getJsonStringValue(dinner, "ingredients", "优质食材搭配"));
                menu.setDinnerCookingMethod(getJsonStringValue(dinner, "cookingMethod", "健康烹饪方式"));
                menu.setDinnerCalories(getJsonDecimalValue(dinner, "calories", new BigDecimal("380")));
                menu.setDinnerNutrition(getJsonStringValue(dinner, "nutrition", "营养均衡"));
            }
            
            // 解析加餐信息
            if (jsonNode.has("snack")) {
                JsonNode snack = jsonNode.get("snack");
                menu.setSnackName(getJsonStringValue(snack, "name", "健康加餐"));
                menu.setSnackDescription(getJsonStringValue(snack, "description", "营养补充加餐"));
                menu.setSnackIngredients(getJsonStringValue(snack, "ingredients", "优质食材搭配"));
                menu.setSnackCookingMethod(getJsonStringValue(snack, "cookingMethod", "健康烹饪方式"));
                menu.setSnackCalories(getJsonDecimalValue(snack, "calories", new BigDecimal("120")));
                menu.setSnackNutrition(getJsonStringValue(snack, "nutrition", "营养均衡"));
            }
            
            // 解析总计信息
            menu.setTotalCalories(getJsonDecimalValue(jsonNode, "totalCalories", new BigDecimal("1300")));
            menu.setAiRecommendationReason(getJsonStringValue(jsonNode, "recommendationReason", "基于年龄、性别和健康状况的个性化推荐"));
            
            log.info("成功解析AI响应JSON，客户ID: {}", customerId);
            
        } catch (Exception e) {
            log.warn("解析AI响应JSON失败，使用文本解析兜底方案，客户ID: {}, 错误: {}", customerId, e.getMessage());
            // JSON解析失败时使用文本解析兜底方案
            parseTextResponse(menu, aiContent);
        }
        
        return menu;
    }
    
    // JSON解析辅助方法
    private String getJsonStringValue(JsonNode node, String fieldName, String defaultValue) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText().trim();
        }
        return defaultValue;
    }
    
    private BigDecimal getJsonDecimalValue(JsonNode node, String fieldName, BigDecimal defaultValue) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            try {
                return new BigDecimal(node.get(fieldName).asText());
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    // 文本解析兜底方案
    private void parseTextResponse(AiPersonalizedMenu menu, String aiContent) {
        // 早餐信息
        menu.setBreakfastName(extractMealName(aiContent, "早餐", "营养早餐"));
        menu.setBreakfastDescription(extractDescription(aiContent, "早餐", "根据个人情况定制的营养早餐"));
        menu.setBreakfastIngredients(extractIngredients(aiContent, "早餐"));
        menu.setBreakfastCookingMethod(extractCookingMethod(aiContent, "早餐"));
        menu.setBreakfastCalories(new BigDecimal("350"));
        menu.setBreakfastNutrition(extractNutritionInfo(aiContent, "早餐"));
        
        // 午餐信息
        menu.setLunchName(extractMealName(aiContent, "午餐", "健康午餐"));
        menu.setLunchDescription(extractDescription(aiContent, "午餐", "营养均衡的午餐搭配"));
        menu.setLunchIngredients(extractIngredients(aiContent, "午餐"));
        menu.setLunchCookingMethod(extractCookingMethod(aiContent, "午餐"));
        menu.setLunchCalories(new BigDecimal("450"));
        menu.setLunchNutrition(extractNutritionInfo(aiContent, "午餐"));
        
        // 晚餐信息
        menu.setDinnerName(extractMealName(aiContent, "晚餐", "清淡晚餐"));
        menu.setDinnerDescription(extractDescription(aiContent, "晚餐", "易消化的晚餐"));
        menu.setDinnerIngredients(extractIngredients(aiContent, "晚餐"));
        menu.setDinnerCookingMethod(extractCookingMethod(aiContent, "晚餐"));
        menu.setDinnerCalories(new BigDecimal("380"));
        menu.setDinnerNutrition(extractNutritionInfo(aiContent, "晚餐"));
        
        // 加餐信息
        menu.setSnackName(extractMealName(aiContent, "加餐", "健康加餐"));
        menu.setSnackDescription(extractDescription(aiContent, "加餐", "营养补充加餐"));
        menu.setSnackIngredients(extractIngredients(aiContent, "加餐"));
        menu.setSnackCookingMethod(extractCookingMethod(aiContent, "加餐"));
        menu.setSnackCalories(new BigDecimal("120"));
        menu.setSnackNutrition(extractNutritionInfo(aiContent, "加餐"));
        
        menu.setTotalCalories(new BigDecimal("1300"));
        menu.setAiRecommendationReason(extractReason(aiContent));
    }
    
    // 文本提取辅助方法（支持餐类型）
    private String extractMealName(String content, String mealType, String defaultValue) {
        String[] patterns = {
            mealType + "名称：",
            mealType + "：",
            "菜品名称：" + mealType
        };
        
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 30;
                return content.substring(start, Math.min(end, content.length())).trim();
            }
        }
        return defaultValue;
    }
    
    private String extractDescription(String content, String mealType, String defaultValue) {
        String[] patterns = {
            mealType + "描述：",
            mealType + "介绍：",
            mealType + "说明："
        };
        
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                return content.substring(start, Math.min(end, content.length())).trim();
            }
        }
        return defaultValue;
    }
    
    private String extractIngredients(String content, String mealType) {
        String[] patterns = {
            mealType + "食材：",
            mealType + "食材清单：",
            "食材：" + mealType
        };
        
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                return content.substring(start, Math.min(end, content.length())).trim();
            }
        }
        return "优质食材搭配";
    }
    
    private String extractCookingMethod(String content, String mealType) {
        String[] patterns = {
            mealType + "制作方法：",
            mealType + "做法：",
            "制作方法：" + mealType
        };
        
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 150;
                return content.substring(start, Math.min(end, content.length())).trim();
            }
        }
        return "健康烹饪方式";
    }
    
    private String extractNutritionInfo(String content, String mealType) {
        String[] patterns = {
            mealType + "营养信息：",
            mealType + "营养：",
            "营养信息：" + mealType
        };
        
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                return content.substring(start, Math.min(end, content.length())).trim();
            }
        }
        return "营养均衡";
    }
    
    private String extractReason(String content) {
        if (content.contains("推荐理由：")) {
            int start = content.indexOf("推荐理由：") + 5;
            int end = content.indexOf("\n", start);
            if (end == -1) end = start + 150;
            return content.substring(start, Math.min(end, content.length())).trim();
        }
        return "AI智能推荐";
    }
    
    private String getTargetGroupDescription(String targetGroup) {
        switch (targetGroup) {
            case "ELDERLY": return "老年人";
            case "DIABETIC": return "糖尿病患者";
            case "HYPERTENSION": return "高血压患者";
            default: return "一般人群";
        }
    }
    
    private String getMealTypeDescription(String mealType) {
        switch (mealType) {
            case "BREAKFAST": return "早餐";
            case "LUNCH": return "午餐";
            case "DINNER": return "晚餐";
            case "SNACK": return "加餐";
            default: return "餐食";
        }
    }
    
    private String getNutritionTypeDescription(String nutritionType) {
        switch (nutritionType) {
            case "HIGH_PROTEIN": return "高蛋白";
            case "LOW_SODIUM": return "低钠";
            case "DIGESTIBLE": return "易消化";
            default: return "营养均衡";
        }
    }
    
    // ========== 历史推荐相关方法 ==========
    
    @Override
    public List<Map<String, Object>> getHistoryRecommendations(Long customerId, String startDate, String endDate) {
        log.info("获取历史推荐记录：客户ID={}, 开始日期={}, 结束日期={}", customerId, startDate, endDate);
        
        LambdaQueryWrapper<AiRecommendationHistory> wrapper = new LambdaQueryWrapper<>();
        
        // 只有当客户ID不为null时才过滤客户
        if (customerId != null) {
            // 这里需要根据实际的表结构来查询，假设历史表中有相关字段
            // wrapper.eq(AiRecommendationHistory::getCustomerId, customerId);
        }
        
        // 只有当日期参数不为null时才过滤日期
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            wrapper.between(AiRecommendationHistory::getAdoptionDate, LocalDate.parse(startDate), LocalDate.parse(endDate));
        } else if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(AiRecommendationHistory::getAdoptionDate, LocalDate.parse(startDate));
        } else if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(AiRecommendationHistory::getAdoptionDate, LocalDate.parse(endDate));
        }
        
        wrapper.orderByDesc(AiRecommendationHistory::getAdoptionDate);
        
        List<AiRecommendationHistory> historyList = historyMapper.selectList(wrapper);
        
        // 转换为Map格式返回
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiRecommendationHistory history : historyList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", history.getId());
            map.put("mealName", history.getMealName());
            map.put("mealType", history.getMealType());
            map.put("targetGroup", history.getTargetGroup());
            map.put("nutritionType", history.getNutritionType());
            map.put("aiScore", history.getAiScore());
            map.put("userRating", history.getUserRating());
            map.put("adoptionDate", history.getAdoptionDate());
            map.put("adoptedByUserName", history.getAdoptedByUserName());
            map.put("effectivenessScore", history.getEffectivenessScore());
            result.add(map);
        }
        
        return result;
    }
    
    @Override
    public List<AiRecommendationTemp> generateFromHistory(Long historyId, Long userId) {
        log.info("基于历史推荐生成类似菜单：历史ID={}, 用户ID={}", historyId, userId);
        
        // 获取历史推荐记录
        AiRecommendationHistory history = historyMapper.selectById(historyId);
        if (history == null) {
            throw new RuntimeException("历史推荐记录不存在");
        }
        
        String sessionId = generateSessionId();
        
        // 构建基于历史的提示词
        String prompt = buildHistoryBasedPrompt(history);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        AiCallLog callLog = new AiCallLog();
        callLog.setSessionId(sessionId);
        callLog.setApiType("HISTORY_BASED");
        callLog.setRequestParams(prompt);
        callLog.setAiModel("deepseek");
        callLog.setRequestTime(apiResponse.getRequestTime());
        callLog.setResponseTime(apiResponse.getResponseTime());
        callLog.setResponseDuration(apiResponse.getResponseDuration());
        callLog.setResponseStatus(apiResponse.getResponseStatus());
        callLog.setErrorMessage(apiResponse.getErrorMessage());
        callLog.setUserId(userId);
        callLog.setCreateTime(LocalDateTime.now());
        callLogMapper.insert(callLog);
        
        // 解析AI响应并生成推荐
        List<AiRecommendationTemp> recommendations = parseAiResponseToRecommendations(
            apiResponse.getParsedContent() != null ? apiResponse.getParsedContent() : apiResponse.getResponseContent(),
            sessionId,
            history.getMealType(),
            history.getNutritionType(),
            history.getTargetGroup(),
            userId
        );
        
        // 标记为基于历史生成
        for (AiRecommendationTemp recommendation : recommendations) {
            recommendation.setIsFromHistory(1);
            recommendation.setHistoryRefId(historyId);
        }
        
        // 批量插入到临时表
        if (!recommendations.isEmpty()) {
            for (AiRecommendationTemp recommendation : recommendations) {
                tempMapper.insert(recommendation);
            }
        }
        
        log.info("基于历史推荐生成完成，生成数量：{}", recommendations.size());
        return recommendations;
    }
    
    @Override
    public AiPersonalizedMenu generateMenuFromHistory(Long historyId, String menuDate, Long userId) {
        log.info("基于历史推荐生成个性化菜单：历史ID={}, 菜单日期={}, 用户ID={}", historyId, menuDate, userId);
        
        // 获取历史推荐记录
        AiRecommendationHistory history = historyMapper.selectById(historyId);
        if (history == null) {
            throw new RuntimeException("历史推荐记录不存在");
        }
        
        String sessionId = generateSessionId();
        
        // 构建基于历史的一日四餐提示词
        String prompt = buildHistoryBasedMenuPrompt(history);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        AiCallLog callLog = new AiCallLog();
        callLog.setSessionId(sessionId);
        callLog.setApiType("HISTORY_MENU");
        callLog.setRequestParams(prompt);
        callLog.setAiModel("deepseek");
        callLog.setRequestTime(apiResponse.getRequestTime());
        callLog.setResponseTime(apiResponse.getResponseTime());
        callLog.setResponseDuration(apiResponse.getResponseDuration());
        callLog.setResponseStatus(apiResponse.getResponseStatus());
        callLog.setErrorMessage(apiResponse.getErrorMessage());
        callLog.setUserId(userId);
        callLog.setCreateTime(LocalDateTime.now());
        callLogMapper.insert(callLog);
        
        // 解析AI响应并生成个性化菜单
        AiPersonalizedMenu menu = parseAiResponseToPersonalizedMenu(
            apiResponse.getParsedContent() != null ? apiResponse.getParsedContent() : apiResponse.getResponseContent(),
            null, // customerId，基于历史可能没有特定客户
            "历史推荐客户",
            null, 
            null, 
            LocalDate.parse(menuDate),
            sessionId,
            "基于历史推荐：" + history.getMealName(),
            "",
            "",
            userId
        );
        
        // 插入到数据库
        menuMapper.insert(menu);
        
        log.info("基于历史推荐的个性化菜单生成完成，菜单ID：{}", menu.getId());
        return menu;
    }
    
    /**
     * 构建基于历史推荐的提示词
     */
    private String buildHistoryBasedPrompt(AiRecommendationHistory history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下历史推荐记录，生成3-5个类似的餐食推荐：\n\n");
        prompt.append("【历史推荐信息】\n");
        prompt.append("菜品名称：").append(history.getMealName()).append("\n");
        prompt.append("餐食类型：").append(getMealTypeDescription(history.getMealType())).append("\n");
        prompt.append("目标群体：").append(getTargetGroupDescription(history.getTargetGroup())).append("\n");
        prompt.append("营养类型：").append(getNutritionTypeDescription(history.getNutritionType())).append("\n");
        prompt.append("AI评分：").append(history.getAiScore()).append("分\n");
        
        if (history.getOriginalAiContent() != null) {
            prompt.append("原始推荐内容：").append(history.getOriginalAiContent()).append("\n");
        }
        
        prompt.append("\n【要求】\n");
        prompt.append("1. 生成的推荐应该与历史记录风格相似\n");
        prompt.append("2. 保持相同的餐食类型和营养特点\n");
        prompt.append("3. 提供创新的食材搭配和制作方法\n");
        prompt.append("4. 确保营养均衡，适合目标群体\n");
        prompt.append("5. 每个推荐包含：菜品名称、描述、主要食材、制作方法、营养信息\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建基于历史推荐的一日四餐提示词
     */
    private String buildHistoryBasedMenuPrompt(AiRecommendationHistory history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下历史推荐记录，为养老院制定一日四餐（早餐、午餐、晚餐、加餐）的完整菜单：\n\n");
        prompt.append("【参考历史推荐】\n");
        prompt.append("菜品名称：").append(history.getMealName()).append("\n");
        prompt.append("餐食类型：").append(getMealTypeDescription(history.getMealType())).append("\n");
        prompt.append("目标群体：").append(getTargetGroupDescription(history.getTargetGroup())).append("\n");
        prompt.append("营养类型：").append(getNutritionTypeDescription(history.getNutritionType())).append("\n");
        
        prompt.append("\n【输出要求】\n");
        prompt.append("请按以下JSON格式返回四餐菜单：\n");
        prompt.append("{\n");
        prompt.append("  \"breakfast\": {\n");
        prompt.append("    \"name\": \"早餐名称\",\n");
        prompt.append("    \"description\": \"早餐描述\",\n");
        prompt.append("    \"ingredients\": \"食材清单\",\n");
        prompt.append("    \"cookingMethod\": \"制作方法\",\n");
        prompt.append("    \"calories\": 热量数值,\n");
        prompt.append("    \"nutrition\": \"营养信息\"\n");
        prompt.append("  },\n");
        prompt.append("  \"lunch\": {\n");
        prompt.append("    \"name\": \"午餐名称\",\n");
        prompt.append("    \"description\": \"午餐描述\",\n");
        prompt.append("    \"ingredients\": \"食材清单\",\n");
        prompt.append("    \"cookingMethod\": \"制作方法\",\n");
        prompt.append("    \"calories\": 热量数值,\n");
        prompt.append("    \"nutrition\": \"营养信息\"\n");
        prompt.append("  },\n");
        prompt.append("  \"dinner\": {\n");
        prompt.append("    \"name\": \"晚餐名称\",\n");
        prompt.append("    \"description\": \"晚餐描述\",\n");
        prompt.append("    \"ingredients\": \"食材清单\",\n");
        prompt.append("    \"cookingMethod\": \"制作方法\",\n");
        prompt.append("    \"calories\": 热量数值,\n");
        prompt.append("    \"nutrition\": \"营养信息\"\n");
        prompt.append("  },\n");
        prompt.append("  \"snack\": {\n");
        prompt.append("    \"name\": \"加餐名称\",\n");
        prompt.append("    \"description\": \"加餐描述\",\n");
        prompt.append("    \"ingredients\": \"食材清单\",\n");
        prompt.append("    \"cookingMethod\": \"制作方法\",\n");
        prompt.append("    \"calories\": 热量数值,\n");
        prompt.append("    \"nutrition\": \"营养信息\"\n");
        prompt.append("  },\n");
        prompt.append("  \"totalCalories\": 总热量,\n");
        prompt.append("  \"recommendationReason\": \"推荐理由\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    @Override
    public List<AiRecommendationTemp> generateFromLatestHistory(Long customerId, Long userId) {
        log.info("基于最新历史记录生成类似推荐：客户ID={}, 用户ID={}", customerId, userId);
        
        // 获取最新的3条历史记录（如果不足3条则全部获取）
        LambdaQueryWrapper<AiRecommendationHistory> wrapper = new LambdaQueryWrapper<>();
        // 不根据客户ID过滤，查询所有历史记录的最新3条
        wrapper.orderByDesc(AiRecommendationHistory::getAdoptionDate)
               .last("LIMIT 3");
        
        List<AiRecommendationHistory> historyList = historyMapper.selectList(wrapper);
        
        if (historyList.isEmpty()) {
            throw new RuntimeException("暂无历史推荐记录，无法生成类似推荐");
        }
        
        String sessionId = generateSessionId();
        
        // 构建基于多条历史记录的提示词
        String prompt = buildMultipleHistoryBasedPrompt(historyList);
        
        // 调用AI API
        AiApiUtil.AiApiResponse apiResponse = aiApiUtil.callDeepSeekApi(prompt);
        
        // 记录API调用日志
        AiCallLog callLog = new AiCallLog();
        callLog.setSessionId(sessionId);
        callLog.setApiType("LATEST_HISTORY_BASED");
        callLog.setRequestParams(prompt);
        callLog.setAiModel("deepseek");
        callLog.setRequestTime(apiResponse.getRequestTime());
        callLog.setResponseTime(apiResponse.getResponseTime());
        callLog.setResponseDuration(apiResponse.getResponseDuration());
        callLog.setResponseStatus(apiResponse.getResponseStatus());
        callLog.setErrorMessage(apiResponse.getErrorMessage());
        callLog.setUserId(userId);
        callLog.setCreateTime(LocalDateTime.now());
        callLogMapper.insert(callLog);
        
        // 解析AI响应并生成推荐（使用第一条历史记录的基本属性）
        AiRecommendationHistory firstHistory = historyList.get(0);
        List<AiRecommendationTemp> recommendations = parseAiResponseToRecommendations(
            apiResponse.getParsedContent() != null ? apiResponse.getParsedContent() : apiResponse.getResponseContent(),
            sessionId,
            firstHistory.getMealType(),
            firstHistory.getNutritionType(),
            firstHistory.getTargetGroup(),
            userId
        );
        
        // 标记为基于最新历史生成
        for (AiRecommendationTemp recommendation : recommendations) {
            recommendation.setIsFromHistory(1);
            recommendation.setHistoryRefId(firstHistory.getId()); // 引用第一条历史记录
        }
        
        // 批量插入到临时表
        if (!recommendations.isEmpty()) {
            for (AiRecommendationTemp recommendation : recommendations) {
                tempMapper.insert(recommendation);
            }
        }
        
        log.info("基于最新历史记录生成推荐完成，使用了{}条历史记录，生成{}条推荐", 
                historyList.size(), recommendations.size());
        return recommendations;
    }
    
    /**
     * 构建基于多条历史记录的提示词
     */
    private String buildMultipleHistoryBasedPrompt(List<AiRecommendationHistory> historyList) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下历史推荐记录，生成1条新的、风格相似但内容不同的餐食推荐：\n\n");
        
        for (int i = 0; i < historyList.size(); i++) {
            AiRecommendationHistory history = historyList.get(i);
            prompt.append("历史记录").append(i + 1).append("：\n");
            prompt.append("菜品名称：").append(history.getMealName()).append("\n");
            prompt.append("餐食类型：").append(history.getMealType()).append("\n");
            prompt.append("目标群体：").append(history.getTargetGroup()).append("\n");
            prompt.append("营养类型：").append(history.getNutritionType()).append("\n");
            if (history.getAiScore() != null) {
                prompt.append("AI评分：").append(history.getAiScore()).append("\n");
            }
            if (history.getUserRating() != null) {
                prompt.append("用户评分：").append(history.getUserRating()).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("要求：\n");
        prompt.append("1. 参考以上历史记录的特点和风格\n");
        prompt.append("2. 生成1条新的、内容不重复的推荐\n");
        prompt.append("3. 保持相似的营养搭配和目标群体\n");
        prompt.append("4. 确保菜品新颖且实用\n\n");
        
        prompt.append("请以JSON格式返回，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"mealName\": \"菜品名称\",\n");
        prompt.append("      \"description\": \"详细描述\",\n");
        prompt.append("      \"mainFood\": \"主食\",\n");
        prompt.append("      \"sideDishes\": \"副菜\",\n");
        prompt.append("      \"soup\": \"汤品\",\n");
        prompt.append("      \"fruits\": \"水果\",\n");
        prompt.append("      \"ingredients\": \"主要食材\",\n");
        prompt.append("      \"cookingMethod\": \"烹饪方法\",\n");
        prompt.append("      \"calories\": 热量数值,\n");
        prompt.append("      \"protein\": 蛋白质含量,\n");
        prompt.append("      \"carbohydrates\": 碳水化合物含量,\n");
        prompt.append("      \"fat\": 脂肪含量,\n");
        prompt.append("      \"fiber\": 纤维含量,\n");
        prompt.append("      \"vitamins\": \"维生素信息\",\n");
        prompt.append("      \"minerals\": \"矿物质信息\",\n");
        prompt.append("      \"specialNutrition\": \"特殊营养成分\",\n");
        prompt.append("      \"aiScore\": AI评分（7-10分）,\n");
        prompt.append("      \"aiReason\": \"推荐理由\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    // ============ 新增的智能文本解析辅助方法 ============
    
    /**
     * 智能提取菜品名称
     */
    private String smartExtractMealName(String content) {
        // 尝试匹配常见的菜品名称模式
        if (content.contains("燕麦南瓜")) return "燕麦南瓜美食套餐";
        if (content.contains("藜麦鸡胸")) return "藜麦鸡胸沙拉套餐";
        
        // 通用模式匹配
        String[] patterns = {"菜品名称：", "推荐菜品：", "名称："};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 30;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (!extracted.isEmpty()) return extracted;
            }
        }
        return null;
    }
    
    /**
     * 智能提取描述信息
     */
    private String smartExtractDescription(String content) {
        String[] patterns = {"描述：", "菜品描述：", "介绍："};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (extracted.length() > 5) return extracted;
            }
        }
        
        // 如果找不到明确描述，尝试提取有意义的句子
        if (content.length() > 20) {
            String[] sentences = content.split("[。！？\\n]");
            for (String sentence : sentences) {
                if (sentence.length() > 10 && sentence.length() < 100 && 
                    (sentence.contains("营养") || sentence.contains("适合") || sentence.contains("搭配"))) {
                    return sentence.trim();
                }
            }
        }
        return null;
    }
    
    /**
     * 智能提取食材信息
     */
    private String smartExtractIngredients(String content) {
        String[] patterns = {"食材：", "主要食材：", "原料：", "配料："};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (!extracted.isEmpty()) return extracted;
            }
        }
        return null;
    }
    
    /**
     * 智能提取烹饪方法
     */
    private String smartExtractCookingMethod(String content) {
        String[] patterns = {"制作方法：", "烹饪方法：", "做法：", "步骤："};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 150;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (extracted.length() > 5) return extracted;
            }
        }
        return null;
    }
    
    /**
     * 智能提取营养信息
     */
    private String smartExtractNutritionInfo(String content) {
        String[] patterns = {"营养信息：", "营养价值：", "营养：", "富含"};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 100;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (!extracted.isEmpty()) return extracted;
            }
        }
        return null;
    }
    
    /**
     * 智能提取推荐理由
     */
    private String smartExtractReasonInfo(String content) {
        String[] patterns = {"推荐理由：", "理由：", "适合原因："};
        for (String pattern : patterns) {
            if (content.contains(pattern)) {
                int start = content.indexOf(pattern) + pattern.length();
                int end = content.indexOf("\n", start);
                if (end == -1) end = start + 150;
                String extracted = content.substring(start, Math.min(end, content.length())).trim();
                if (!extracted.isEmpty()) return extracted;
            }
        }
        return null;
    }
    
    /**
     * 智能提取热量信息
     */
    private BigDecimal smartExtractCalories(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*千卡|热量.*?(\\d+)|calories.*?(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    try {
                        return new BigDecimal(group);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 智能提取蛋白质信息
     */
    private BigDecimal smartExtractProtein(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("蛋白质.*?(\\d+(?:\\.\\d+)?)|protein.*?(\\d+(?:\\.\\d+)?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    try {
                        return new BigDecimal(group);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 智能提取碳水化合物信息
     */
    private BigDecimal smartExtractCarbohydrate(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("碳水化合物.*?(\\d+(?:\\.\\d+)?)|carbohydrate.*?(\\d+(?:\\.\\d+)?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    try {
                        return new BigDecimal(group);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 智能提取脂肪信息
     */
    private BigDecimal smartExtractFat(String content) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("脂肪.*?(\\d+(?:\\.\\d+)?)|fat.*?(\\d+(?:\\.\\d+)?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    try {
                        return new BigDecimal(group);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 创建兜底推荐
     */
    private AiRecommendationTemp createFallbackRecommendation(String sessionId, String mealType, 
                                                             String nutritionType, String targetGroup, Long userId) {
        AiRecommendationTemp temp = new AiRecommendationTemp();
        temp.setSessionId(sessionId);
        temp.setMealName("AI营养推荐");
        temp.setMealType(mealType);
        temp.setDescription("基于AI分析的营养搭配餐食");
        temp.setIngredients("优质食材搭配");
        temp.setCookingMethod("健康烹饪方式");
        temp.setNutritionInfo("营养均衡搭配");
        temp.setTargetGroup(targetGroup);
        temp.setNutritionType(nutritionType);
        temp.setAiScore(new BigDecimal("7.5"));
        temp.setAiReason("AI智能营养推荐");
        temp.setAiModel("deepseek");
        temp.setIsFromHistory(0);
        temp.setUserId(userId);
        temp.setCreateTime(LocalDateTime.now());
        temp.setCalories(BigDecimal.ZERO);
        temp.setProtein(BigDecimal.ZERO);
        temp.setCarbohydrate(BigDecimal.ZERO);
        temp.setFat(BigDecimal.ZERO);
        return temp;
    }
    
    /**
     * 尝试从部分或破损的JSON中提取信息
     */
    private List<AiRecommendationTemp> extractFromPartialJson(String aiContent, String sessionId, 
                                                             String mealType, String nutritionType, 
                                                             String targetGroup, Long userId) {
        List<AiRecommendationTemp> recommendations = new ArrayList<>();
        
        try {
            // 尝试找到JSON部分
            int jsonStart = aiContent.indexOf("{");
            int jsonEnd = aiContent.lastIndexOf("}");
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonPart = aiContent.substring(jsonStart, jsonEnd + 1);
                
                // 简单的JSON修复
                jsonPart = jsonPart.replaceAll("```json", "").replaceAll("```", "");
                jsonPart = jsonPart.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
                
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonPart);
                
                JsonNode recommendationsNode = rootNode.get("recommendations");
                if (recommendationsNode != null && recommendationsNode.isArray()) {
                    for (JsonNode recNode : recommendationsNode) {
                        AiRecommendationTemp temp = createRecommendationFromJsonNode(recNode, sessionId, mealType, nutritionType, targetGroup, userId);
                        recommendations.add(temp);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("部分JSON提取失败", e);
        }
        
        return recommendations;
    }
    
    /**
     * 从JSON节点创建推荐对象
     */
    private AiRecommendationTemp createRecommendationFromJsonNode(JsonNode recNode, String sessionId, 
                                                                 String mealType, String nutritionType, 
                                                                 String targetGroup, Long userId) {
        AiRecommendationTemp temp = new AiRecommendationTemp();
        temp.setSessionId(sessionId);
        temp.setMealName(getJsonString(recNode, "mealName", "AI推荐菜品"));
        temp.setMealType(mealType);
        temp.setDescription(getJsonString(recNode, "description", "营养搭配餐食"));
        temp.setMainFood(getJsonString(recNode, "mainFood", ""));
        temp.setSideDishes(getJsonString(recNode, "sideDishes", ""));
        temp.setSoup(getJsonString(recNode, "soup", ""));
        temp.setFruits(getJsonString(recNode, "fruits", ""));
        temp.setIngredients(getJsonString(recNode, "ingredients", "优质食材搭配"));
        temp.setCookingMethod(getJsonString(recNode, "cookingMethod", "健康烹饪方式"));
        temp.setCalories(getJsonBigDecimal(recNode, "calories", BigDecimal.ZERO));
        temp.setProtein(getJsonBigDecimal(recNode, "protein", BigDecimal.ZERO));
        temp.setFat(getJsonBigDecimal(recNode, "fat", BigDecimal.ZERO));
        temp.setCarbohydrate(getJsonBigDecimal(recNode, "carbohydrate", BigDecimal.ZERO));
        temp.setNutritionInfo(getJsonString(recNode, "nutritionInfo", "营养均衡"));
        temp.setSuitableCrowd(getJsonString(recNode, "suitableCrowd", ""));
        temp.setTargetGroup(targetGroup);
        temp.setNutritionType(nutritionType);
        temp.setEstimatedCost(getJsonBigDecimal(recNode, "estimatedCost", BigDecimal.ZERO));
        temp.setAiScore(getJsonBigDecimal(recNode, "aiScore", new BigDecimal("8.0")));
        temp.setAiReason(getJsonString(recNode, "aiReason", "AI智能推荐"));
        temp.setAiModel("deepseek");
        temp.setIsFromHistory(0);
        temp.setUserId(userId);
        temp.setCreateTime(LocalDateTime.now());
        return temp;
    }
}
