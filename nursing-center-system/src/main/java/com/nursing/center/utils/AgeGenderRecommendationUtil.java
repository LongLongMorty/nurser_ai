package com.nursing.center.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于年龄和性别的推荐工具类
 */
public class AgeGenderRecommendationUtil {

    /**
     * 根据年龄获取推荐策略
     * @param age 年龄
     * @return 年龄推荐策略
     */
    public static Map<String, Object> getAgeBasedRecommendation(Integer age) {
        Map<String, Object> recommendation = new HashMap<>();
        
        if (age == null) {
            age = 70; // 默认70岁
        }
        
        if (age < 60) {
            // 中青年（50-60岁）
            recommendation.put("caloriesRange", "1800-2200");
            recommendation.put("proteinRatio", "18-20%");
            recommendation.put("fatRatio", "25-30%");
            recommendation.put("carbRatio", "50-55%");
            recommendation.put("cookingStyle", "适中口感，营养均衡");
            recommendation.put("specialNeeds", "预防慢性疾病，增强免疫力");
            recommendation.put("mealSize", "正常分量");
            recommendation.put("texture", "正常口感");
        } else if (age < 75) {
            // 初老年（60-75岁）
            recommendation.put("caloriesRange", "1600-2000");
            recommendation.put("proteinRatio", "20-22%");
            recommendation.put("fatRatio", "20-25%");
            recommendation.put("carbRatio", "55-60%");
            recommendation.put("cookingStyle", "易消化，低盐低脂");
            recommendation.put("specialNeeds", "护心养胃，防止骨质疏松");
            recommendation.put("mealSize", "适中分量");
            recommendation.put("texture", "软烂适中");
        } else if (age < 85) {
            // 中老年（75-85岁）
            recommendation.put("caloriesRange", "1400-1800");
            recommendation.put("proteinRatio", "22-25%");
            recommendation.put("fatRatio", "15-20%");
            recommendation.put("carbRatio", "55-60%");
            recommendation.put("cookingStyle", "软烂易嚼，清淡营养");
            recommendation.put("specialNeeds", "易消化，补充钙质和维生素");
            recommendation.put("mealSize", "小份多餐");
            recommendation.put("texture", "软烂");
        } else {
            // 高龄老年（85岁以上）
            recommendation.put("caloriesRange", "1200-1600");
            recommendation.put("proteinRatio", "25-30%");
            recommendation.put("fatRatio", "15-18%");
            recommendation.put("carbRatio", "50-55%");
            recommendation.put("cookingStyle", "流质半流质，精细营养");
            recommendation.put("specialNeeds", "高蛋白易吸收，防止营养不良");
            recommendation.put("mealSize", "少量多餐");
            recommendation.put("texture", "软糯流质");
        }
        
        return recommendation;
    }

    /**
     * 根据性别获取推荐策略
     * @param gender 性别（MALE-男性，FEMALE-女性）
     * @return 性别推荐策略
     */
    public static Map<String, Object> getGenderBasedRecommendation(String gender) {
        Map<String, Object> recommendation = new HashMap<>();
        
        if ("MALE".equalsIgnoreCase(gender) || "男".equals(gender)) {
            // 男性推荐
            recommendation.put("caloriesBonus", 100); // 额外热量
            recommendation.put("proteinBonus", 5); // 额外蛋白质
            recommendation.put("preferredFoods", "红肉、鱼类、豆制品");
            recommendation.put("avoidFoods", "过甜食物");
            recommendation.put("specialNeeds", "前列腺保健，心血管保护");
            recommendation.put("supplements", "锌、硒、维生素E");
        } else if ("FEMALE".equalsIgnoreCase(gender) || "女".equals(gender)) {
            // 女性推荐
            recommendation.put("caloriesBonus", -50); // 减少热量
            recommendation.put("calciumBonus", 20); // 额外钙质
            recommendation.put("preferredFoods", "鱼类、豆制品、绿叶蔬菜");
            recommendation.put("avoidFoods", "高脂肪食物");
            recommendation.put("specialNeeds", "骨质疏松预防，贫血预防");
            recommendation.put("supplements", "钙、铁、叶酸、维生素D");
        } else {
            // 默认推荐
            recommendation.put("caloriesBonus", 0);
            recommendation.put("proteinBonus", 0);
            recommendation.put("preferredFoods", "均衡饮食");
            recommendation.put("avoidFoods", "");
            recommendation.put("specialNeeds", "营养均衡");
            recommendation.put("supplements", "综合维生素");
        }
        
        return recommendation;
    }

    /**
     * 生成完整的个性化推荐提示词
     * @param customerName 客户姓名
     * @param age 年龄
     * @param gender 性别
     * @param healthConditions 健康状况
     * @param dietaryPreferences 饮食偏好
     * @param allergies 过敏信息
     * @param mealType 餐食类型（如果是单餐推荐）
     * @return 个性化提示词
     */
    public static String generatePersonalizedPrompt(String customerName, Integer age, String gender,
                                                   String healthConditions, String dietaryPreferences, 
                                                   String allergies, String mealType) {
        Map<String, Object> ageRec = getAgeBasedRecommendation(age);
        Map<String, Object> genderRec = getGenderBasedRecommendation(gender);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为养老院的入住人员").append(customerName != null ? customerName : "").append("制定个性化餐食推荐。\n\n");
        
        // 基本信息
        prompt.append("【客户基本信息】\n");
        prompt.append("年龄：").append(age != null ? age + "岁" : "未知").append("\n");
        prompt.append("性别：").append(gender != null ? gender : "未知").append("\n");
        
        // 年龄相关推荐
        prompt.append("\n【年龄相关营养需求】\n");
        prompt.append("热量范围：").append(ageRec.get("caloriesRange")).append("卡路里/天\n");
        prompt.append("蛋白质比例：").append(ageRec.get("proteinRatio")).append("\n");
        prompt.append("烹饪要求：").append(ageRec.get("cookingStyle")).append("\n");
        prompt.append("特殊需求：").append(ageRec.get("specialNeeds")).append("\n");
        prompt.append("食物质地：").append(ageRec.get("texture")).append("\n");
        
        // 性别相关推荐
        prompt.append("\n【性别相关营养建议】\n");
        prompt.append("推荐食物：").append(genderRec.get("preferredFoods")).append("\n");
        prompt.append("特殊关注：").append(genderRec.get("specialNeeds")).append("\n");
        prompt.append("营养补充：").append(genderRec.get("supplements")).append("\n");
        
        // 健康状况
        if (healthConditions != null && !healthConditions.trim().isEmpty()) {
            prompt.append("\n【健康状况】\n").append(healthConditions).append("\n");
        }
        
        // 饮食偏好
        if (dietaryPreferences != null && !dietaryPreferences.trim().isEmpty()) {
            prompt.append("\n【饮食偏好】\n").append(dietaryPreferences).append("\n");
        }
        
        // 过敏信息
        if (allergies != null && !allergies.trim().isEmpty()) {
            prompt.append("\n【过敏禁忌】\n").append("严格避免：").append(allergies).append("\n");
        }
        
        // 推荐要求
        if (mealType != null && !mealType.trim().isEmpty()) {
            prompt.append("\n【推荐要求】\n");
            prompt.append("餐食类型：").append(getMealTypeDesc(mealType)).append("\n");
            prompt.append("请针对此餐食类型，结合以上个人信息，推荐具体的菜品搭配。\n");
        } else {
            prompt.append("\n【推荐要求】\n");
            prompt.append("请制定完整的一天四餐（早餐、午餐、晚餐、加餐）计划，包括：\n");
            prompt.append("1. 具体菜品名称和制作方法\n");
            prompt.append("2. 营养成分分析\n");
            prompt.append("3. 热量计算\n");
            prompt.append("4. 个性化调整说明\n");
        }
        
        prompt.append("\n请确保推荐符合养老院饮食标准，安全营养，易于制作和消化。");
        
        return prompt.toString();
    }

    /**
     * 获取餐食类型描述
     */
    private static String getMealTypeDesc(String mealType) {
        switch (mealType.toUpperCase()) {
            case "BREAKFAST": return "早餐";
            case "LUNCH": return "午餐";
            case "DINNER": return "晚餐";
            case "SNACK": return "加餐";
            default: return mealType;
        }
    }

    /**
     * 根据年龄性别调整热量需求
     * @param baseCalories 基础热量
     * @param age 年龄
     * @param gender 性别
     * @return 调整后的热量
     */
    public static int adjustCaloriesByAgeGender(int baseCalories, Integer age, String gender) {
        Map<String, Object> ageRec = getAgeBasedRecommendation(age);
        Map<String, Object> genderRec = getGenderBasedRecommendation(gender);
        
        int adjustedCalories = baseCalories;
        
        // 性别调整
        Integer genderBonus = (Integer) genderRec.get("caloriesBonus");
        if (genderBonus != null) {
            adjustedCalories += genderBonus;
        }
        
        // 年龄调整（从热量范围中取中值）
        String caloriesRange = (String) ageRec.get("caloriesRange");
        if (caloriesRange != null && caloriesRange.contains("-")) {
            String[] range = caloriesRange.split("-");
            try {
                int minCalories = Integer.parseInt(range[0]);
                int maxCalories = Integer.parseInt(range[1]);
                int avgCalories = (minCalories + maxCalories) / 2;
                
                // 如果调整后的热量超出合理范围，则使用年龄推荐的平均值
                if (adjustedCalories < minCalories || adjustedCalories > maxCalories) {
                    adjustedCalories = avgCalories;
                }
            } catch (NumberFormatException e) {
                // 解析失败，使用原值
            }
        }
        
        return Math.max(adjustedCalories, 1000); // 最低不少于1000卡
    }

    /**
     * 生成一日四餐菜单的AI提示词
     * @param age 年龄
     * @param gender 性别
     * @param healthConditions 健康状况
     * @param dietaryPreferences 饮食偏好
     * @param allergies 过敏信息
     * @return 一日四餐提示词
     */
    public static String generateDailyMenuPrompt(Integer age, String gender, String healthConditions, 
                                                String dietaryPreferences, String allergies) {
        Map<String, Object> ageRec = getAgeBasedRecommendation(age);
        Map<String, Object> genderRec = getGenderBasedRecommendation(gender);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为养老院制定一日四餐（早餐、午餐、晚餐、加餐）的完整菜单。\n\n");
        
        // 基本信息
        prompt.append("【客户基本信息】\n");
        prompt.append("年龄：").append(age != null ? age + "岁" : "未知").append("\n");
        prompt.append("性别：").append(gender != null ? ("MALE".equals(gender) ? "男性" : "女性") : "未知").append("\n");
        
        // 健康状况
        if (healthConditions != null && !healthConditions.trim().isEmpty()) {
            prompt.append("健康状况：").append(healthConditions).append("\n");
        }
        
        // 饮食偏好
        if (dietaryPreferences != null && !dietaryPreferences.trim().isEmpty()) {
            prompt.append("饮食偏好：").append(dietaryPreferences).append("\n");
        }
        
        // 过敏信息
        if (allergies != null && !allergies.trim().isEmpty()) {
            prompt.append("过敏信息：").append(allergies).append("\n");
        }
        
        // 年龄相关推荐
        prompt.append("\n【年龄营养建议】\n");
        prompt.append("热量需求：").append(ageRec.get("caloriesRange")).append("千卡/天\n");
        prompt.append("蛋白质比例：").append(ageRec.get("proteinRatio")).append("\n");
        prompt.append("脂肪比例：").append(ageRec.get("fatRatio")).append("\n");
        prompt.append("碳水化合物比例：").append(ageRec.get("carbRatio")).append("\n");
        prompt.append("烹饪建议：").append(ageRec.get("cookingStyle")).append("\n");
        prompt.append("特殊需求：").append(ageRec.get("specialNeeds")).append("\n");
        
        // 性别相关推荐
        prompt.append("\n【性别营养建议】\n");
        prompt.append("推荐食物：").append(genderRec.get("preferredFoods")).append("\n");
        prompt.append("特别需求：").append(genderRec.get("specialNeeds")).append("\n");
        prompt.append("推荐补充：").append(genderRec.get("supplements")).append("\n");
        
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
}
