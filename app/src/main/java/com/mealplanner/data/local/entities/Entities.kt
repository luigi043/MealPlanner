package com.mealplanner.data.local.entities

import androidx.room.*
import com.mealplanner.domain.model.RecipeCategory

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val instructions: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val category: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val isFavorite: Boolean = false,
    val imageUri: String? = null,
    val tags: String = "", // Comma-separated tags
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val quantity: Double,
    val unit: String
)

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<IngredientEntity>
)

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val mealType: String,
    val recipeId: Long,
    val recipeName: String,
    val recipeCalories: Double,
    val recipeProtein: Double,
    val recipeCarbs: Double,
    val recipeFat: Double,
    val servings: Int = 1,
    val notes: String = ""
)

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isChecked: Boolean = false,
    val category: String = "Other",
    val isManuallyAdded: Boolean = false
)