package com.nursing.center.service;

import com.nursing.center.entity.AiRecommendationTemp;
import com.nursing.center.entity.AiRecommendationHistory;
import com.nursing.center.entity.AiPersonalizedMenu;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * AI推荐服务接口
 * 提供AI智能食谱推荐的核心功能
 */
public interface AiRecommendationService {
    
    // ============ 功能1：实时AI推荐 + 采纳到正式表 ============
    
    /**
     * 根据条件生成AI推荐
     */
    List<AiRecommendationTemp> generateRecommendations(String mealType, String nutritionType, 
                                                      String targetGroup, String additionalRequirements, 
                                                      Integer[] caloriesRange, Long userId);
    
    /**
     * 获取当前会话的推荐列表
     */
    List<AiRecommendationTemp> getCurrentRecommendations(String sessionId);
    
    /**
     * 采纳推荐（写入meal_config表）
     */
    Long adoptRecommendation(Long tempId, Long userId);
    
    /**
     * 批量采纳推荐
     */
    List<Long> batchAdoptRecommendations(List<Long> tempIds, Long userId);
    
    /**
     * 清空当前会话的推荐
     */
    void clearCurrentRecommendations(String sessionId);
    
    // ============ 功能2：基于历史采纳记录的智能推荐 ============
    
    /**
     * 检查是否有历史采纳记录
     */
    boolean hasAdoptionHistory(String mealType, String targetGroup);
    
    /**
     * 基于历史记录生成推荐
     */
    List<AiRecommendationTemp> generateFromHistory(String mealType, String nutritionType, 
                                                  String targetGroup, Long userId);
    
    /**
     * 获取采纳历史记录
     */
    List<AiRecommendationHistory> getAdoptionHistory(String mealType, String targetGroup, Integer limit);
    
    // ============ 功能3：为特定客户生成个性化餐食（年龄性别） ============
    
    /**
     * 为客户生成一天的个性化菜单（四餐）- 支持年龄性别
     */
    Long generatePersonalizedMenuWithAgeGender(Long customerId, String customerName, Integer age, String gender,
                                              LocalDate menuDate, String healthConditions, String dietaryPreferences,
                                              String allergies, Long userId);
    
    /**
     * 根据客户年龄性别生成个性化推荐
     */
    List<AiRecommendationTemp> generatePersonalizedRecommendations(Long customerId, String customerName,
                                                                  Integer age, String gender, String mealType,
                                                                  String nutritionType, String targetGroup,
                                                                  String healthConditions, String dietaryPreferences,
                                                                  String allergies, Long userId);
    
    /**
     * 获取客户的个性化菜单
     */
    AiPersonalizedMenu getPersonalizedMenu(Long customerId, LocalDate menuDate);
    
    /**
     * 获取客户的菜单历史
     */
    List<AiPersonalizedMenu> getPersonalizedMenuHistory(Long customerId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 审核个性化菜单
     */
    void reviewPersonalizedMenu(Long menuId, Integer isApproved, String reviewComment, Long reviewerId);
    
    // ============ 获取客户信息并推荐 ============
    
    /**
     * 获取客户信息并生成推荐
     */
    List<AiRecommendationTemp> generateRecommendationsForCustomer(Long customerId, String mealType,
                                                                 String nutritionType, String targetGroup, Long userId);
    
    // ============ 新增方法（为Controller提供支持）============
    
    /**
     * 生成客户个性化菜单（直接返回菜单对象）
     */
    AiPersonalizedMenu generatePersonalizedMenu(Long customerId, String customerName, Integer age, String gender,
                                               LocalDate menuDate, String healthConditions, String dietaryPreferences,
                                               String allergies);
    
    /**
     * 获取客户的个性化菜单列表
     */
    List<AiPersonalizedMenu> getCustomerPersonalizedMenus(Long customerId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 审核个性化菜单
     */
    void approvePersonalizedMenu(Long menuId, String reviewComment);
    
    /**
     * 获取AI调用统计信息
     */
    Map<String, Object> getAiCallStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 生成新的会话ID
     */
    String generateSessionId();
    
    /**
     * 调用DeepSeek API生成推荐
     */
    String callDeepSeekApi(String prompt);
    
    /**
     * 获取历史推荐记录
     */
    List<Map<String, Object>> getHistoryRecommendations(Long customerId, String startDate, String endDate);
    
    /**
     * 基于历史推荐生成类似菜单
     */
    List<AiRecommendationTemp> generateFromHistory(Long historyId, Long userId);
    
    /**
     * 基于历史推荐生成个性化菜单
     */
    AiPersonalizedMenu generateMenuFromHistory(Long historyId, String menuDate, Long userId);
    
    /**
     * 基于最新历史记录生成类似推荐（最多取3条最新记录）
     */
    List<AiRecommendationTemp> generateFromLatestHistory(Long customerId, Long userId);
}
