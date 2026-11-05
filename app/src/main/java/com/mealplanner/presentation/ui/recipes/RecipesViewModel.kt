package com.mealplanner.presentation.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealplanner.data.repository.RecipeRepository
import com.mealplanner.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipesUiState(
    val isLoading: Boolean = true,
    val recipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: RecipeCategory = RecipeCategory.ALL,
    val showFavoritesOnly: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(RecipeCategory.ALL)
    private val _showFavoritesOnly = MutableStateFlow(false)

    val uiState: StateFlow<RecipesUiState> = combine(
        _searchQuery,
        _selectedCategory,
        _showFavoritesOnly
    ) { query, category, favOnly -> Triple(query, category, favOnly) }
        .flatMapLatest { (query, category, favOnly) ->
            val source = repository.getAllRecipes()
            source.map { recipes ->
                RecipesUiState(
                    isLoading = false,
                    recipes = filterRecipes(recipes, query, category, favOnly),
                    searchQuery = query,
                    selectedCategory = category,
                    showFavoritesOnly = favOnly
                )
            }
        }
        .catch { e -> emit(RecipesUiState(isLoading = false, error = e.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecipesUiState())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onCategorySelected(category: RecipeCategory) { _selectedCategory.value = category }
    fun onToggleFavorites() { _showFavoritesOnly.value = !_showFavoritesOnly.value }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id, !recipe.isFavorite)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }
}

// ─── Add/Edit Recipe ViewModel ────────────────────────────────────────────────

data class AddEditRecipeUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val recipe: Recipe? = null,
    val name: String = "",
    val description: String = "",
    val instructions: String = "",
    val prepTime: String = "",
    val cookTime: String = "",
    val servings: String = "2",
    val category: RecipeCategory = RecipeCategory.DINNER,
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val fiber: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val tags: String = "",
    val isSaved: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

@HiltViewModel
class AddEditRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditRecipeUiState())
    val uiState: StateFlow<AddEditRecipeUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val recipe = repository.getRecipeById(recipeId)
            if (recipe != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        recipe = recipe,
                        name = recipe.name,
                        description = recipe.description,
                        instructions = recipe.instructions,
                        prepTime = recipe.prepTimeMinutes.toString(),
                        cookTime = recipe.cookTimeMinutes.toString(),
                        servings = recipe.servings.toString(),
                        category = recipe.category,
                        calories = recipe.calories.toString(),
                        protein = recipe.protein.toString(),
                        carbs = recipe.carbs.toString(),
                        fat = recipe.fat.toString(),
                        fiber = recipe.fiber.toString(),
                        ingredients = recipe.ingredients,
                        tags = recipe.tags.joinToString(", ")
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onInstructionsChange(value: String) = _uiState.update { it.copy(instructions = value) }
    fun onPrepTimeChange(value: String) = _uiState.update { it.copy(prepTime = value) }
    fun onCookTimeChange(value: String) = _uiState.update { it.copy(cookTime = value) }
    fun onServingsChange(value: String) = _uiState.update { it.copy(servings = value) }
    fun onCategoryChange(value: RecipeCategory) = _uiState.update { it.copy(category = value) }
    fun onCaloriesChange(value: String) = _uiState.update { it.copy(calories = value) }
    fun onProteinChange(value: String) = _uiState.update { it.copy(protein = value) }
    fun onCarbsChange(value: String) = _uiState.update { it.copy(carbs = value) }
    fun onFatChange(value: String) = _uiState.update { it.copy(fat = value) }
    fun onFiberChange(value: String) = _uiState.update { it.copy(fiber = value) }
    fun onTagsChange(value: String) = _uiState.update { it.copy(tags = value) }

    fun addIngredient(name: String, quantity: String, unit: String) {
        if (name.isBlank() || unit.isBlank() || !validAmount(quantity, allowZero = false)) return
        val qty = quantity.toDouble()
        val ingredient = Ingredient(name = name.trim(), quantity = qty, unit = unit.trim())
        _uiState.update { it.copy(ingredients = it.ingredients + ingredient) }
    }

    fun removeIngredient(ingredient: Ingredient) {
        _uiState.update { it.copy(ingredients = it.ingredients - ingredient) }
    }

    fun saveRecipe() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading || state.isSaved) return
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
            val recipe = Recipe(
                id = state.recipe?.id ?: 0L,
                name = state.name.trim(),
                description = state.description.trim(),
                instructions = state.instructions.trim(),
                prepTimeMinutes = state.prepTime.toIntOrNull() ?: 0,
                cookTimeMinutes = state.cookTime.toIntOrNull() ?: 0,
                servings = state.servings.toIntOrNull() ?: 1,
                category = state.category,
                calories = state.calories.toDoubleOrNull() ?: 0.0,
                protein = state.protein.toDoubleOrNull() ?: 0.0,
                carbs = state.carbs.toDoubleOrNull() ?: 0.0,
                fat = state.fat.toDoubleOrNull() ?: 0.0,
                fiber = state.fiber.toDoubleOrNull() ?: 0.0,
                ingredients = state.ingredients,
                tags = state.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                isFavorite = state.recipe?.isFavorite ?: false,
                createdAt = state.recipe?.createdAt ?: System.currentTimeMillis(),
                imageUri = state.recipe?.imageUri
            )
            if (state.recipe == null) {
                repository.insertRecipe(recipe)
            } else {
                repository.updateRecipe(recipe)
            }
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errors = mapOf("save" to "Could not save recipe. Please try again.")) }
            }
        }
    }

    private fun validate(state: AddEditRecipeUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.name.isBlank()) errors["name"] = "Recipe name is required"
        if (state.instructions.isBlank()) errors["instructions"] = "Instructions are required"
        if (!validAmount(state.calories)) errors["calories"] = "Enter a finite, non-negative number"
        listOf("protein" to state.protein, "carbs" to state.carbs, "fat" to state.fat, "fiber" to state.fiber).forEach { (key, value) ->
            if (value.isNotBlank() && !validAmount(value)) errors[key] = "Enter a finite, non-negative number"
        }
        listOf("prepTime" to state.prepTime, "cookTime" to state.cookTime).forEach { (key, value) ->
            if (value.isNotBlank() && (value.toIntOrNull() == null || (value.toIntOrNull() ?: -1) < 0)) errors[key] = "Enter whole minutes, 0 or more"
        }
        if ((state.servings.toIntOrNull() ?: 0) !in 1..1000) errors["servings"] = "Enter 1 to 1000 servings"
        if (state.ingredients.isEmpty()) errors["ingredients"] = "Add at least one ingredient"
        return errors
    }
}

// ─── Recipe Detail ViewModel ──────────────────────────────────────────────────

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val scaledServings: Int = 1,
    val error: String? = null
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            repository.getRecipeByIdFlow(recipeId)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .collect { recipe ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recipe = recipe,
                            scaledServings = recipe?.servings ?: 1
                        )
                    }
                }
        }
    }

    fun onServingsChange(servings: Int) {
        if (servings > 0) _uiState.update { it.copy(scaledServings = servings) }
    }

    fun toggleFavorite() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id, !recipe.isFavorite)
        }
    }
}
