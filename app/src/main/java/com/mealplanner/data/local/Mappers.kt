package com.mealplanner.data.local

import com.mealplanner.data.local.entities.*
import com.mealplanner.domain.model.*
import java.time.LocalDate

// ─── Recipe Mappers ───────────────────────────────────────────────────────────

fun RecipeWithIngredients.toDomain(): Recipe = recipe.toDomain(ingredients)

fun RecipeEntity.toDomain(ingredients: List<IngredientEntity>): Recipe = Recipe(
    id = id,
    name = name,
    description = description,
    ingredients = ingredients.map { it.toDomain() },
    instructions = instructions,
    prepTimeMinutes = prepTimeMinutes,
    cookTimeMinutes = cookTimeMinutes,
    servings = servings,
    category = RecipeCategory.entries.find { it.name == category } ?: RecipeCategory.DINNER,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    isFavorite = isFavorite,
    imageUri = imageUri,
    tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
    createdAt = createdAt
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    name = name,
    description = description,
    instructions = instructions,
    prepTimeMinutes = prepTimeMinutes,
    cookTimeMinutes = cookTimeMinutes,
    servings = servings,
    category = category.name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    isFavorite = isFavorite,
    imageUri = imageUri,
    tags = tags.joinToString(","),
    createdAt = createdAt
)

// ─── Ingredient Mappers ───────────────────────────────────────────────────────

fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    recipeId = recipeId,
    name = name,
    quantity = quantity,
    unit = unit
)

fun Ingredient.toEntity(recipeId: Long): IngredientEntity = IngredientEntity(
    id = id,
    recipeId = recipeId,
    name = name,
    quantity = quantity,
    unit = unit
)

// ─── MealPlan Mappers ─────────────────────────────────────────────────────────

fun MealPlanEntity.toDomain(): MealPlan = MealPlan(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    mealType = MealType.entries.find { it.name == mealType } ?: MealType.DINNER,
    recipeId = recipeId,
    recipeName = recipeName,
    recipeCalories = recipeCalories,
    recipeProtein = recipeProtein,
    recipeCarbs = recipeCarbs,
    recipeFat = recipeFat,
    servings = servings,
    notes = notes
)

fun MealPlan.toEntity(): MealPlanEntity = MealPlanEntity(
    id = id,
    dateEpochDay = date.toEpochDay(),
    mealType = mealType.name,
    recipeId = recipeId,
    recipeName = recipeName,
    recipeCalories = recipeCalories,
    recipeProtein = recipeProtein,
    recipeCarbs = recipeCarbs,
    recipeFat = recipeFat,
    servings = servings,
    notes = notes
)

// ─── Grocery Mappers ──────────────────────────────────────────────────────────

fun GroceryItemEntity.toDomain(): GroceryItem = GroceryItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    isChecked = isChecked,
    category = category,
    isManuallyAdded = isManuallyAdded
)

fun GroceryItem.toEntity(): GroceryItemEntity = GroceryItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    isChecked = isChecked,
    category = category,
    isManuallyAdded = isManuallyAdded
)
