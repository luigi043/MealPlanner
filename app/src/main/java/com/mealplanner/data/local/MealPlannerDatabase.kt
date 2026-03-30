package com.mealplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mealplanner.data.local.dao.*
import com.mealplanner.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        MealPlanEntity::class,
        GroceryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MealPlannerDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryDao(): GroceryDao

    companion object {
        const val DATABASE_NAME = "meal_planner_db"

        fun prepopulateCallback(scope: CoroutineScope) = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    // Pre-population is handled via SeedDataProvider
                }
            }
        }
    }
}