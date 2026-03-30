package com.mealplanner.domain.model

import java.time.LocalDate

data class Recipe(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val category: RecipeCategory,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val isFavorite: Boolean = false,
    val imageUri: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Ingredient(
    val id: Long = 0,
    val recipeId: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String
)

data class MealPlan(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val recipeId: Long,
    val recipeName: String,
    val recipeCalories: Double,
    val recipeProtein: Double,
    val recipeCarbs: Double,
    val recipeFat: Double,
    val servings: Int = 1,
    val notes: String = ""
)

data class GroceryItem(
    val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isChecked: Boolean = false,
    val category: String = "Other",
    val isManuallyAdded: Boolean = false
)

data class NutritionSummary(
    val date: LocalDate,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val mealCount: Int
)

data class WeeklyNutritionSummary(
    val startDate: LocalDate,
    val dailySummaries: List<NutritionSummary>,
    val avgCalories: Double,
    val avgProtein: Double,
    val avgCarbs: Double,
    val avgFat: Double,
    val totalCalories: Double
)

data class SmartSuggestion(
    val recipe: Recipe,
    val reason: String,
    val matchScore: Double
)

enum class MealType(val displayName: String, val emoji: String, val order: Int) {
    BREAKFAST("Breakfast", "🌅", 0),
    LUNCH("Lunch", "☀️", 1),
    DINNER("Dinner", "🌙", 2),
    SNACK("Snack", "🍎", 3);

    companion object {
        fun fromDisplayName(name: String): MealType =
            values().find { it.displayName == name } ?: BREAKFAST
    }
}

enum class RecipeCategory(val displayName: String, val emoji: String) {
    ALL("All", "🍽️"),
    BREAKFAST("Breakfast", "🥐"),
    LUNCH("Lunch", "🥗"),
    DINNER("Dinner", "🍝"),
    SNACK("Snack", "🍎"),
    DESSERT("Dessert", "🍰"),
    VEGETARIAN("Vegetarian", "🥦"),
    VEGAN("Vegan", "🌱"),
    KETO("Keto", "🥑"),
    PALEO("Paleo", "🍖"),
    GLUTEN_FREE("Gluten Free", "🌾"),
    HIGH_PROTEIN("High Protein", "💪"),
    LOW_CARB("Low Carb", "📉"),
    QUICK("Quick & Easy", "⚡")
}

data class UserGoals(
    val dailyCalorieTarget: Int = 2000,
    val dailyProteinTarget: Int = 150,
    val dailyCarbsTarget: Int = 250,
    val dailyFatTarget: Int = 65,
    val planningGoal: PlanningGoal = PlanningGoal.BALANCED
)

enum class PlanningGoal(val displayName: String) {
    WEIGHT_LOSS("Weight Loss"),
    MUSCLE_GAIN("Muscle Gain"),
    BALANCED("Balanced Diet"),
    KETO("Keto Diet"),
    MAINTENANCE("Maintenance")
}