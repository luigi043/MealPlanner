package com.mealplanner.data.repository

import com.mealplanner.data.local.*
import com.mealplanner.data.local.dao.*
import com.mealplanner.domain.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// ─── Recipe Repository ────────────────────────────────────────────────────────

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
        .map { entities -> entities.map { it.toDomain() } }

    fun getFavoriteRecipes(): Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()
        .map { entities -> entities.map { it.toDomain() } }

    fun getRecipesByCategory(category: RecipeCategory): Flow<List<Recipe>> {
        return if (category == RecipeCategory.ALL) {
            getAllRecipes()
        } else {
            recipeDao.getRecipesByCategory(category.name).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun getRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)?.toDomain()
    }

    fun getRecipeByIdFlow(id: Long): Flow<Recipe?> = recipeDao.getRecipeByIdFlow(id)
        .map { it?.toDomain() }

    suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = recipeDao.insertRecipe(recipe.toEntity())
        val ingredientEntities = recipe.ingredients.map { it.toEntity(recipeId) }
        ingredientDao.insertIngredients(ingredientEntities)
        return recipeId
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe.toEntity())
        ingredientDao.deleteIngredientsByRecipeId(recipe.id)
        val ingredientEntities = recipe.ingredients.map { it.toEntity(recipe.id) }
        ingredientDao.insertIngredients(ingredientEntities)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe.toEntity())
    }

    suspend fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }

    suspend fun getRecipeCount(): Int = recipeDao.getRecipeCount()
}

// ─── MealPlan Repository ──────────────────────────────────────────────────────

@Singleton
class MealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao
) {
    fun getAllMealPlans(): Flow<List<MealPlan>> =
        mealPlanDao.getAllMealPlans().map { it.map { e -> e.toDomain() } }

    fun getMealPlansByDate(date: LocalDate): Flow<List<MealPlan>> =
        mealPlanDao.getMealPlansByDate(date.toEpochDay()).map { it.map { e -> e.toDomain() } }

    fun getMealPlansByDateRange(start: LocalDate, end: LocalDate): Flow<List<MealPlan>> =
        mealPlanDao.getMealPlansByDateRange(start.toEpochDay(), end.toEpochDay())
            .map { it.map { e -> e.toDomain() } }

    suspend fun insertMealPlan(mealPlan: MealPlan): Long =
        mealPlanDao.insertMealPlan(mealPlan.toEntity())

    suspend fun updateMealPlan(mealPlan: MealPlan) =
        mealPlanDao.updateMealPlan(mealPlan.toEntity())

    suspend fun deleteMealPlan(mealPlan: MealPlan) =
        mealPlanDao.deleteMealPlan(mealPlan.toEntity())

    fun getDailyCalories(date: LocalDate): Flow<Double> =
        mealPlanDao.getDailyCalories(date.toEpochDay()).map { it ?: 0.0 }

    suspend fun getDatesWithPlansInRange(start: LocalDate, end: LocalDate): List<LocalDate> =
        mealPlanDao.getDatesWithPlans(start.toEpochDay(), end.toEpochDay())
            .map { LocalDate.ofEpochDay(it) }
}

// ─── Grocery Repository ───────────────────────────────────────────────────────

@Singleton
class GroceryRepository @Inject constructor(
    private val groceryDao: GroceryDao
) {
    fun getAllGroceryItems(): Flow<List<GroceryItem>> =
        groceryDao.getAllGroceryItems().map { it.map { e -> e.toDomain() } }

    fun getPendingItemCount(): Flow<Int> = groceryDao.getPendingItemCount()

    suspend fun insertGroceryItem(item: GroceryItem): Long =
        groceryDao.insertGroceryItem(item.toEntity())

    suspend fun insertGroceryItems(items: List<GroceryItem>) =
        groceryDao.insertGroceryItems(items.map { it.toEntity() })

    suspend fun updateGroceryItem(item: GroceryItem) =
        groceryDao.updateGroceryItem(item.toEntity())

    suspend fun toggleChecked(id: Long, isChecked: Boolean) =
        groceryDao.updateCheckedStatus(id, isChecked)

    suspend fun deleteGroceryItem(item: GroceryItem) =
        groceryDao.deleteGroceryItem(item.toEntity())

    suspend fun deleteCheckedItems() = groceryDao.deleteCheckedItems()

    suspend fun clearAll() = groceryDao.clearAllGroceryItems()
}
