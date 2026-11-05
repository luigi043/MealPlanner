package com.mealplanner.domain.usecase

import com.mealplanner.data.repository.GroceryRepository
import com.mealplanner.data.repository.MealPlanRepository
import com.mealplanner.data.repository.RecipeRepository
import com.mealplanner.domain.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

// ─── Generate Grocery List ────────────────────────────────────────────────────

class GenerateGroceryListUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate) {
        val mealPlans = mealPlanRepository.getMealPlansByDateRange(startDate, endDate).first()
        val ingredientMap = mutableMapOf<String, Pair<Double, String>>()

        for (plan in mealPlans) {
            val recipe = recipeRepository.getRecipeById(plan.recipeId) ?: continue
            val scaleFactor = ingredientScale(plan.servings, recipe.servings)

            for (ingredient in recipe.ingredients) {
                val key = "${ingredient.name.lowercase().trim()}|${ingredient.unit.lowercase().trim()}"
                val scaled = ingredient.quantity * scaleFactor
                val existing = ingredientMap[key]
                if (existing != null) {
                    ingredientMap[key] = Pair(existing.first + scaled, ingredient.unit)
                } else {
                    ingredientMap[key] = Pair(scaled, ingredient.unit)
                }
            }
        }

        val groceryItems = ingredientMap.map { (key, quantityUnit) ->
            val name = key.split("|")[0].replaceFirstChar { it.uppercase() }
            GroceryItem(
                name = name,
                quantity = quantityUnit.first,
                unit = quantityUnit.second,
                category = categorizeIngredient(name)
            )
        }

        groceryRepository.replaceGeneratedItems(groceryItems)
    }

    private fun categorizeIngredient(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.containsAny("chicken", "beef", "pork", "fish", "salmon", "tuna", "shrimp", "turkey", "lamb") -> "Meat & Fish"
            lower.containsAny("milk", "cheese", "yogurt", "butter", "cream", "egg") -> "Dairy & Eggs"
            lower.containsAny("bread", "flour", "rice", "pasta", "oat", "cereal", "quinoa") -> "Grains & Bread"
            lower.containsAny("apple", "banana", "orange", "berry", "lemon", "lime", "grape", "mango") -> "Fruits"
            lower.containsAny("carrot", "broccoli", "spinach", "tomato", "onion", "garlic", "pepper", "lettuce", "kale") -> "Vegetables"
            lower.containsAny("oil", "vinegar", "sauce", "ketchup", "mustard", "soy") -> "Condiments & Oils"
            lower.containsAny("sugar", "honey", "maple", "chocolate", "vanilla") -> "Baking & Sweets"
            lower.containsAny("salt", "pepper", "cumin", "oregano", "basil", "thyme", "cinnamon", "paprika") -> "Spices & Herbs"
            lower.containsAny("bean", "lentil", "chickpea", "tofu", "tempeh") -> "Plant Proteins"
            lower.containsAny("almond", "walnut", "cashew", "peanut", "seed") -> "Nuts & Seeds"
            else -> "Other"
        }
    }

    private fun String.containsAny(vararg keywords: String) = keywords.any { this.contains(it) }
}

// ─── Get Nutrition Summary ────────────────────────────────────────────────────

class GetNutritionSummaryUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {
    fun getDailySummary(date: LocalDate): Flow<NutritionSummary> =
        mealPlanRepository.getMealPlansByDate(date).map { plans ->
            NutritionSummary(
                date = date,
                totalCalories = plans.sumOf { it.recipeCalories * it.servings },
                totalProtein = plans.sumOf { it.recipeProtein * it.servings },
                totalCarbs = plans.sumOf { it.recipeCarbs * it.servings },
                totalFat = plans.sumOf { it.recipeFat * it.servings },
                mealCount = plans.size
            )
        }

    fun getWeeklySummary(startDate: LocalDate): Flow<WeeklyNutritionSummary> {
        val endDate = startDate.plusDays(6)
        return mealPlanRepository.getMealPlansByDateRange(startDate, endDate).map { plans ->
            val dailyMap = plans.groupBy { it.date }
            val dailySummaries = (0..6).map { offset ->
                val date = startDate.plusDays(offset.toLong())
                val dayPlans = dailyMap[date] ?: emptyList()
                NutritionSummary(
                    date = date,
                    totalCalories = dayPlans.sumOf { it.recipeCalories * it.servings },
                    totalProtein = dayPlans.sumOf { it.recipeProtein * it.servings },
                    totalCarbs = dayPlans.sumOf { it.recipeCarbs * it.servings },
                    totalFat = dayPlans.sumOf { it.recipeFat * it.servings },
                    mealCount = dayPlans.size
                )
            }
            val daysWithMeals = dailySummaries.filter { it.mealCount > 0 }
            val count = daysWithMeals.size.coerceAtLeast(1)
            WeeklyNutritionSummary(
                startDate = startDate,
                dailySummaries = dailySummaries,
                avgCalories = daysWithMeals.sumOf { it.totalCalories } / count,
                avgProtein = daysWithMeals.sumOf { it.totalProtein } / count,
                avgCarbs = daysWithMeals.sumOf { it.totalCarbs } / count,
                avgFat = daysWithMeals.sumOf { it.totalFat } / count,
                totalCalories = dailySummaries.sumOf { it.totalCalories }
            )
        }
    }
}

// ─── Smart Meal Suggestions ───────────────────────────────────────────────────

class GetSmartSuggestionsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository
) {
    fun getSuggestions(
        date: LocalDate,
        mealType: MealType,
        calorieTarget: Int
    ): Flow<List<SmartSuggestion>> {
        return combine(
            recipeRepository.getAllRecipes(),
            mealPlanRepository.getMealPlansByDate(date)
        ) { recipes, existingPlans ->
            val consumedCalories = existingPlans.sumOf { it.recipeCalories * it.servings }
            val remainingCalories = calorieTarget - consumedCalories
            val plannedRecipeIds = existingPlans.map { it.recipeId }.toSet()

            recipes
                .filter { it.id !in plannedRecipeIds }
                .map { recipe ->
                    val calorieDiff = Math.abs(recipe.calories - remainingCalories / (4 - existingPlans.size).coerceAtLeast(1))
                    val calorieScore = (1.0 - (calorieDiff / calorieTarget.coerceAtLeast(1))).coerceIn(0.0, 1.0)
                    val categoryBonus = if (recipe.category.name.lowercase() == mealType.name.lowercase()) 0.3 else 0.0
                    val favoriteBonus = if (recipe.isFavorite) 0.2 else 0.0
                    val score = (calorieScore * 0.5 + categoryBonus + favoriteBonus).coerceIn(0.0, 1.0)

                    val reason = when {
                        recipe.isFavorite -> "⭐ One of your favorites"
                        calorieScore > 0.8 -> "Close to your remaining calorie target"
                        recipe.prepTimeMinutes <= 20 -> "⚡ Quick to prepare"
                        recipe.category == RecipeCategory.HIGH_PROTEIN -> "💪 Great for protein intake"
                        else -> "✨ Recommended for you"
                    }
                    SmartSuggestion(recipe, reason, score)
                }
                .sortedByDescending { it.matchScore }
                .take(5)
        }
    }
}

// ─── Auto Generate Meal Plan ──────────────────────────────────────────────────

class AutoGenerateMealPlanUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(
        startDate: LocalDate,
        goals: UserGoals
    ) {
        val recipes = recipeRepository.getAllRecipes().first()
        if (recipes.isEmpty()) return

        val existing = mealPlanRepository.getMealPlansByDateRange(startDate, startDate.plusDays(6)).first()
        val occupiedSlots = existing.map { it.date to it.mealType }.toSet()
        val shuffled = recipes.shuffled()
        var recipeIndex = 0

        fun nextRecipe(mealType: MealType): Recipe {
            // Try to find a recipe matching the meal type category
            val typeFiltered = shuffled.filter {
                it.category.name.lowercase() == mealType.name.lowercase()
            }
            return if (typeFiltered.isNotEmpty()) {
                typeFiltered[recipeIndex % typeFiltered.size]
            } else {
                shuffled[recipeIndex % shuffled.size]
            }.also { recipeIndex++ }
        }

        for (dayOffset in 0..6) {
            val date = startDate.plusDays(dayOffset.toLong())
            val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
            for (mealType in mealTypes) {
                if ((date to mealType) in occupiedSlots) continue
                val recipe = nextRecipe(mealType)
                mealPlanRepository.insertMealPlan(
                    MealPlan(
                        date = date,
                        mealType = mealType,
                        recipeId = recipe.id,
                        recipeName = recipe.name,
                        recipeCalories = recipe.calories,
                        recipeProtein = recipe.protein,
                        recipeCarbs = recipe.carbs,
                        recipeFat = recipe.fat,
                        servings = 1
                    )
                )
            }
        }
    }
}
