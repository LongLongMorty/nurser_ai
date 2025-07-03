package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import com.nursing.center.entity.AiRecommendationTemp;
import com.nursing.center.entity.AiPersonalizedMenu;
import com.nursing.center.service.AiRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * AI推荐控制器
 */
@RestController
@RequestMapping("/api/ai-recommendation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    /**
     * 生成通用AI推荐
     */
    @PostMapping("/generate")
    public Result<List<AiRecommendationTemp>> generateRecommendations(@Valid @RequestBody GenerateRecommendationRequest request) {
        try {
            List<AiRecommendationTemp> recommendations = aiRecommendationService.generateRecommendations(
                request.getMealType(),
                request.getNutritionType(),
                request.getTargetGroup(),
                request.getAdditionalRequirements(),
                request.getCaloriesRange(),
                1L // 默认用户ID，实际项目中应从session或token获取
            );
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("生成通用推荐失败", e);
            return Result.error("生成推荐失败: " + e.getMessage());
        }
    }

    /**
     * 生成个性化AI推荐
     */
    @PostMapping("/personalized")
    public Result<List<AiRecommendationTemp>> generatePersonalizedRecommendations(@Valid @RequestBody PersonalizedRecommendationRequest request) {
        try {
            List<AiRecommendationTemp> recommendations = aiRecommendationService.generatePersonalizedRecommendations(
                request.getCustomerId(),
                request.getCustomerName(),
                request.getCustomerAge(),
                request.getCustomerGender(),
                request.getMealType(),
                request.getNutritionType(),
                request.getTargetGroup(),
                request.getHealthConditions(),
                request.getDietaryPreferences(),
                request.getAllergies(),
                1L // 默认用户ID
            );
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("生成个性化推荐失败", e);
            return Result.error("生成个性化推荐失败: " + e.getMessage());
        }
    }

    /**
     * 生成客户一日四餐个性化菜单
     */
    @PostMapping("/personalized-menu/generate")
    public Result<AiPersonalizedMenu> generatePersonalizedMenu(@Valid @RequestBody GeneratePersonalizedMenuRequest request) {
        try {
            LocalDate menuDate = LocalDate.parse(request.getMenuDate());
            AiPersonalizedMenu menu = aiRecommendationService.generatePersonalizedMenu(
                request.getCustomerId(),
                request.getCustomerName(),
                request.getCustomerAge(),
                request.getCustomerGender(),
                menuDate,
                request.getHealthConditions(),
                request.getDietaryPreferences(),
                request.getAllergies()
            );
            return Result.success(menu);
        } catch (Exception e) {
            log.error("生成一日四餐失败", e);
            return Result.error("生成一日四餐失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前推荐列表
     */
    @GetMapping("/current")
    public Result<List<AiRecommendationTemp>> getCurrentRecommendations(@RequestParam(required = false) String sessionId) {
        try {
            List<AiRecommendationTemp> recommendations = aiRecommendationService.getCurrentRecommendations(sessionId);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取当前推荐失败", e);
            return Result.error("获取当前推荐失败: " + e.getMessage());
        }
    }

    /**
     * 采纳推荐到meal_config表
     */
    @PostMapping("/adopt/{recommendationId}")
    public Result<Long> adoptRecommendation(@PathVariable Long recommendationId) {
        try {
            Long mealConfigId = aiRecommendationService.adoptRecommendation(recommendationId, 1L);
            return Result.success(mealConfigId);
        } catch (Exception e) {
            log.error("采纳推荐失败", e);
            return Result.error("采纳推荐失败: " + e.getMessage());
        }
    }

    /**
     * 批量采纳推荐
     */
    @PostMapping("/adopt/batch")
    public Result<List<Long>> batchAdoptRecommendations(@Valid @RequestBody BatchAdoptRequest request) {
        try {
            List<Long> mealConfigIds = aiRecommendationService.batchAdoptRecommendations(request.getRecommendationIds(), 1L);
            return Result.success(mealConfigIds);
        } catch (Exception e) {
            log.error("批量采纳推荐失败", e);
            return Result.error("批量采纳推荐失败: " + e.getMessage());
        }
    }

    /**
     * 清空当前推荐列表
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCurrentRecommendations(@RequestParam(required = false) String sessionId) {
        try {
            aiRecommendationService.clearCurrentRecommendations(sessionId);
            return Result.success();
        } catch (Exception e) {
            log.error("清空推荐失败", e);
            return Result.error("清空推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户的个性化菜单历史
     */
    @GetMapping("/personalized-menu/customer/{customerId}")
    public Result<List<AiPersonalizedMenu>> getCustomerPersonalizedMenus(
            @PathVariable Long customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            List<AiPersonalizedMenu> menus = aiRecommendationService.getCustomerPersonalizedMenus(customerId, start, end);
            return Result.success(menus);
        } catch (Exception e) {
            log.error("获取客户菜单历史失败", e);
            return Result.error("获取菜单历史失败: " + e.getMessage());
        }
    }

    /**
     * 审核个性化菜单
     */
    @PostMapping("/personalized-menu/{menuId}/approve")
    public Result<Void> approvePersonalizedMenu(@PathVariable Long menuId, @RequestBody ApproveMenuRequest request) {
        try {
            aiRecommendationService.approvePersonalizedMenu(menuId, request.getReview());
            return Result.success();
        } catch (Exception e) {
            log.error("审核菜单失败", e);
            return Result.error("审核菜单失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI调用统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAiCallStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            Map<String, Object> statistics = aiRecommendationService.getAiCallStatistics(start, end);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取历史推荐记录
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistoryRecommendations(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> history = aiRecommendationService.getHistoryRecommendations(customerId, startDate, endDate);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取历史推荐失败", e);
            return Result.error("获取历史推荐失败: " + e.getMessage());
        }
    }

    /**
     * 基于历史推荐生成类似菜单
     */
    @PostMapping("/generate-from-history/{historyId}")
    public Result<List<AiRecommendationTemp>> generateFromHistory(@PathVariable Long historyId) {
        try {
            List<AiRecommendationTemp> recommendations = aiRecommendationService.generateFromHistory(historyId, 1L);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("基于历史生成推荐失败", e);
            return Result.error("基于历史生成推荐失败: " + e.getMessage());
        }
    }

    /**
     * 基于历史推荐生成个性化菜单
     */
    @PostMapping("/personalized-menu/generate-from-history/{historyId}")
    public Result<AiPersonalizedMenu> generateMenuFromHistory(
            @PathVariable Long historyId,
            @RequestBody GenerateMenuFromHistoryRequest request) {
        try {
            AiPersonalizedMenu menu = aiRecommendationService.generateMenuFromHistory(historyId, request.getMenuDate(), 1L);
            return Result.success(menu);
        } catch (Exception e) {
            log.error("基于历史生成菜单失败", e);
            return Result.error("基于历史生成菜单失败: " + e.getMessage());
        }
    }

    /**
     * 基于最新历史记录生成类似推荐
     */
    @PostMapping("/generate-from-latest-history")
    public Result<List<AiRecommendationTemp>> generateFromLatestHistory(
            @RequestParam(required = false) Long customerId) {
        try {
            List<AiRecommendationTemp> recommendations = aiRecommendationService.generateFromLatestHistory(customerId, 1L);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("基于最新历史生成推荐失败", e);
            return Result.error("基于最新历史生成推荐失败: " + e.getMessage());
        }
    }

    // ==================== 请求DTO类 ====================
    
    @lombok.Data
    public static class GenerateRecommendationRequest {
        private String mealType;
        private String targetGroup;
        private String nutritionType;
        private String additionalRequirements;
        private Integer[] caloriesRange; // 热量范围 [最小值, 最大值]
        private Integer count = 5;
    }

    @lombok.Data
    public static class PersonalizedRecommendationRequest {
        private Long customerId;
        private String customerName;
        private Integer customerAge;
        private String customerGender;
        private String mealType;
        private String targetGroup;
        private String nutritionType;
        private String healthConditions;
        private String dietaryPreferences;
        private String allergies;
        private Integer count = 4;
    }

    @lombok.Data
    public static class GeneratePersonalizedMenuRequest {
        private Long customerId;
        private String customerName;
        private Integer customerAge;
        private String customerGender;
        private String menuDate;
        private String healthConditions;
        private String dietaryPreferences;
        private String allergies;
    }

    @lombok.Data
    public static class BatchAdoptRequest {
        private List<Long> recommendationIds;
    }

    @lombok.Data
    public static class ApproveMenuRequest {
        private String review;
    }

    @lombok.Data
    public static class GenerateMenuFromHistoryRequest {
        private String menuDate;
    }
}
