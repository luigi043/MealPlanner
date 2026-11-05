package com.mealplanner.domain.model

/** Numeric rules shared by forms and regression tests. Nutrition is per serving. */
fun validAmount(value: String, allowZero: Boolean = true): Boolean {
    val number = value.toDoubleOrNull() ?: return false
    return number.isFinite() && if (allowZero) number >= 0 else number > 0
}

fun filterRecipes(recipes: List<Recipe>, query: String, category: RecipeCategory, favoritesOnly: Boolean): List<Recipe> =
    recipes.filter { recipe ->
        (category == RecipeCategory.ALL || recipe.category == category) &&
            (!favoritesOnly || recipe.isFavorite) &&
            (query.isBlank() || recipe.name.contains(query.trim(), ignoreCase = true) ||
                recipe.tags.any { it.contains(query.trim(), ignoreCase = true) })
    }

fun ingredientScale(servings: Int, recipeServings: Int): Double =
    servings.coerceAtLeast(1).toDouble() / recipeServings.coerceAtLeast(1)
