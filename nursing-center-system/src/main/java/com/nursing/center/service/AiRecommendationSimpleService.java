package com.nursing.center.service;

import com.nursing.center.entity.AiRecommendationTemp;
import com.nursing.center.entity.AiRecommendationHistory;
import com.nursing.center.entity.AiPersonalizedMenu;
import java.time.LocalDate;
import java.util.List;

/**
 * AI推荐服务接口 - 简化版
 * 基于用户需求的核心功能
 */
public interface AiRecommendationSimpleService {
    
    // ============ 功能1：实时AI推荐 + 采纳到正式表 ============
    
    /**
     * 根据条件生成AI推荐
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param additionalRequirements 额外要求
     * @param userId 用户ID
     * @return 推荐结果列表
     */
    List<AiRecommendationTemp> generateRecommendations(String mealType, String nutritionType, 
                                                      String targetGroup, String additionalRequirements, Long userId);
    
    /**
     * 根据用户年龄和性别生成个性化推荐
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param additionalRequirements 额外要求
     * @param userId 用户ID
     * @param userAge 用户年龄
     * @param userGender 用户性别
     * @return 个性化推荐结果列表
     */
    List<AiRecommendationTemp> generatePersonalizedRecommendations(String mealType, String nutritionType, 
                                                                  String targetGroup, String additionalRequirements, 
                                                                  Long userId, Integer userAge, String userGender);
    
    /**
     * 获取当前会话的推荐列表
     * @param sessionId 会话ID
     * @return 推荐列表
     */
    List<AiRecommendationTemp> getCurrentRecommendations(String sessionId);
    
    /**
     * 采纳推荐（写入meal_config表）
     * @param tempId 临时推荐ID
     * @param userId 用户ID
     * @return meal_config表中的新ID
     */
    Long adoptRecommendation(Long tempId, Long userId);
    
    /**
     * 批量采纳推荐
     * @param tempIds 临时推荐ID列表
     * @param userId 用户ID
     * @return 成功采纳的meal_config ID列表
     */
    List<Long> batchAdoptRecommendations(List<Long> tempIds, Long userId);
    
    /**
     * 清空当前会话的推荐
     * @param sessionId 会话ID
     */
    void clearCurrentRecommendations(String sessionId);
    
    // ============ 功能2：基于历史采纳记录的智能推荐 ============
    
    /**
     * 检查是否有历史采纳记录
     * @param mealType 菜品类型（可选）
     * @param targetGroup 目标人群（可选）
     * @return 是否有历史记录
     */
    boolean hasAdoptionHistory(String mealType, String targetGroup);
    
    /**
     * 基于历史记录生成推荐
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param userId 用户ID
     * @return 基于历史的推荐列表
     */
    List<AiRecommendationTemp> generateFromHistory(String mealType, String nutritionType, 
                                                  String targetGroup, Long userId);
    
    /**
     * 基于历史记录和用户特征生成个性化推荐
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param userId 用户ID
     * @param userAge 用户年龄
     * @param userGender 用户性别
     * @return 基于历史的个性化推荐列表
     */
    List<AiRecommendationTemp> generatePersonalizedFromHistory(String mealType, String nutritionType, 
                                                              String targetGroup, Long userId, 
                                                              Integer userAge, String userGender);
    
    /**
     * 获取采纳历史记录
     * @param mealType 菜品类型（可选）
     * @param targetGroup 目标人群（可选）
     * @param limit 限制数量
     * @return 历史记录列表
     */
    List<AiRecommendationHistory> getAdoptionHistory(String mealType, String targetGroup, Integer limit);
    
    // ============ 功能3：为特定客户生成个性化餐食 ============
    
    /**
     * 为客户生成一天的个性化菜单（四餐）
     * @param customerId 客户ID
     * @param customerAge 客户年龄
     * @param customerGender 客户性别
     * @param menuDate 菜单日期
     * @param healthConditions 健康状况
     * @param dietaryPreferences 饮食偏好
     * @param allergies 过敏信息
     * @param userId 操作用户ID
     * @return 个性化菜单ID
     */
    Long generatePersonalizedMenu(Long customerId, Integer customerAge, String customerGender, 
                                 LocalDate menuDate, String healthConditions,
                                 String dietaryPreferences, String allergies, Long userId);
    
    /**
     * 获取客户的个性化菜单
     * @param customerId 客户ID
     * @param menuDate 菜单日期
     * @return 个性化菜单
     */
    AiPersonalizedMenu getPersonalizedMenu(Long customerId, LocalDate menuDate);
    
    /**
     * 获取客户的菜单历史
     * @param customerId 客户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 菜单历史列表
     */
    List<AiPersonalizedMenu> getPersonalizedMenuHistory(Long customerId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 审核个性化菜单
     * @param menuId 菜单ID
     * @param isApproved 是否通过（1-通过，2-拒绝）
     * @param reviewComment 审核意见
     * @param reviewerId 审核者ID
     */
    void reviewPersonalizedMenu(Long menuId, Integer isApproved, String reviewComment, Long reviewerId);
    
    // ============ 个性化推荐相关方法 ============
    
    /**
     * 根据客户年龄性别生成个性化推荐
     * @param customerId 客户ID
     * @param customerName 客户姓名
     * @param age 年龄
     * @param gender 性别
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param healthConditions 健康状况
     * @param dietaryPreferences 饮食偏好
     * @param allergies 过敏信息
     * @param userId 用户ID
     * @return 个性化推荐列表
     */
    List<AiRecommendationTemp> generatePersonalizedRecommendations(Long customerId, String customerName,
                                                                  Integer age, String gender, String mealType,
                                                                  String nutritionType, String targetGroup,
                                                                  String healthConditions, String dietaryPreferences,
                                                                  String allergies, Long userId);
    
    /**
     * 为客户生成一天的个性化菜单（四餐）- 增强版
     * @param customerId 客户ID
     * @param customerName 客户姓名  
     * @param age 年龄
     * @param gender 性别
     * @param menuDate 菜单日期
     * @param healthConditions 健康状况
     * @param dietaryPreferences 饮食偏好
     * @param allergies 过敏信息
     * @param userId 操作用户ID
     * @return 个性化菜单ID
     */
    Long generatePersonalizedMenuEnhanced(Long customerId, String customerName, Integer age, String gender,
                                         LocalDate menuDate, String healthConditions, String dietaryPreferences,
                                         String allergies, Long userId);
    
    /**
     * 获取客户信息并生成推荐
     * @param customerId 客户ID
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param userId 用户ID
     * @return 个性化推荐列表
     */
    List<AiRecommendationTemp> generateRecommendationsForCustomer(Long customerId, String mealType,
                                                                 String nutritionType, String targetGroup, Long userId);
    
    // ============ 辅助方法 ============
    
    /**
     * 生成新的会话ID
     * @return 会话ID
     */
    String generateSessionId();
    
    /**
     * 调用DeepSeek API生成推荐
     * @param prompt 提示词
     * @return AI响应
     */
    String callDeepSeekApi(String prompt);
    
    /**
     * 构建个性化提示词
     * @param mealType 菜品类型
     * @param nutritionType 营养要求
     * @param targetGroup 适用人群
     * @param userAge 用户年龄
     * @param userGender 用户性别
     * @param additionalRequirements 额外要求
     * @return 个性化提示词
     */
    String buildPersonalizedPrompt(String mealType, String nutritionType, String targetGroup, 
                                  Integer userAge, String userGender, String additionalRequirements);
}
