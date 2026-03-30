package com.mealplanner.util

import com.mealplanner.data.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    fun seedIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = recipeRepository.getRecipeCount()
            if (count == 0) {
                SeedDataProvider.getSampleRecipes().forEach { recipe ->
                    recipeRepository.insertRecipe(recipe)
                }
            }
        }
    }
}