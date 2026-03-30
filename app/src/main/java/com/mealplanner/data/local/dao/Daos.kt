package com.mealplanner.data.local.dao

import androidx.room.*
import com.mealplanner.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ─── Recipe DAO ────────────────────────────────────────────────────────────────

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteRecipes(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY name ASC")
    fun getRecipesByCategory(category: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipes(query: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeWithIngredients?

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeByIdFlow(id: Long): Flow<RecipeWithIngredients?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int
}

// ─── Ingredient DAO ────────────────────────────────────────────────────────────

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId")
    fun getIngredientsByRecipeId(recipeId: Long): Flow<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)
}

// ─── Meal Plan DAO ─────────────────────────────────────────────────────────────

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plans ORDER BY dateEpochDay ASC, mealType ASC")
    fun getAllMealPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE dateEpochDay = :epochDay ORDER BY mealType ASC")
    fun getMealPlansByDate(epochDay: Long): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE dateEpochDay BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpochDay ASC, mealType ASC")
    fun getMealPlansByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlanEntity): Long

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlanEntity)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlanEntity)

    @Query("SELECT SUM(recipeCalories * servings) FROM meal_plans WHERE dateEpochDay = :epochDay")
    fun getDailyCalories(epochDay: Long): Flow<Double?>

    @Query("SELECT DISTINCT dateEpochDay FROM meal_plans WHERE dateEpochDay BETWEEN :startEpoch AND :endEpoch")
    suspend fun getDatesWithPlans(startEpoch: Long, endEpoch: Long): List<Long>
}

// ─── Grocery DAO ───────────────────────────────────────────────────────────────

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_items ORDER BY isChecked ASC, category ASC, name ASC")
    fun getAllGroceryItems(): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItems(items: List<GroceryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity): Long

    @Query("UPDATE grocery_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun updateCheckedStatus(id: Long, isChecked: Boolean)

    @Delete
    suspend fun deleteGroceryItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("DELETE FROM grocery_items")
    suspend fun clearAllGroceryItems()

    @Query("SELECT COUNT(*) FROM grocery_items WHERE isChecked = 0")
    fun getPendingItemCount(): Flow<Int>
}