package com.mealplanner.presentation.ui.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealplanner.data.repository.MealPlanRepository
import com.mealplanner.data.repository.RecipeRepository
import com.mealplanner.domain.model.*
import com.mealplanner.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

data class MealPlanUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStart: LocalDate = LocalDate.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1),
    val dayMeals: Map<MealType, List<MealPlan>> = emptyMap(),
    val weekMeals: Map<LocalDate, List<MealPlan>> = emptyMap(),
    val availableRecipes: List<Recipe> = emptyList(),
    val suggestions: List<SmartSuggestion> = emptyList(),
    val datesWithPlans: List<LocalDate> = emptyList(),
    val isGenerating: Boolean = false,
    val showWeekView: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val generateGroceryListUseCase: GenerateGroceryListUseCase,
    private val getSmartSuggestionsUseCase: GetSmartSuggestionsUseCase,
    private val autoGenerateMealPlanUseCase: AutoGenerateMealPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private val selectedDate = MutableStateFlow(LocalDate.now())

    init {
        viewModelScope.launch {
            selectedDate.flatMapLatest { date ->
                val weekStart = date.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                combine(
                    mealPlanRepository.getMealPlansByDate(date),
                    mealPlanRepository.getMealPlansByDateRange(weekStart, weekStart.plusDays(6)),
                    recipeRepository.getAllRecipes()
                ) { dayMeals, weekMeals, recipes ->
                    val grouped = dayMeals.groupBy { it.mealType }
                    val weekGrouped = weekMeals.groupBy { it.date }
                    val datesWithPlans = weekGrouped.keys.toList()
                    Triple(grouped, weekGrouped.mapValues { it.value }, datesWithPlans to recipes)
                }.map { (grouped, weekGrouped, rest) ->
                    val (datesWithPlans, recipes) = rest
                    _uiState.value.copy(
                        isLoading = false,
                        selectedDate = date,
                        weekStart = date.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1),
                        dayMeals = grouped,
                        weekMeals = weekGrouped,
                        availableRecipes = recipes,
                        datesWithPlans = datesWithPlans,
                        error = null
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun addMeal(date: LocalDate, mealType: MealType, recipe: Recipe, servings: Int = 1) {
        viewModelScope.launch {
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
                    servings = servings
                )
            )
        }
    }

    fun removeMeal(mealPlan: MealPlan) {
        viewModelScope.launch {
            mealPlanRepository.deleteMealPlan(mealPlan)
        }
    }

    fun generateGroceryList() {
        viewModelScope.launch {
            val weekStart = _uiState.value.weekStart
            generateGroceryListUseCase(weekStart, weekStart.plusDays(6))
        }
    }

    fun autoGenerateWeekPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            autoGenerateMealPlanUseCase(
                startDate = _uiState.value.weekStart,
                goals = UserGoals()
            )
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    fun loadSmartSuggestions(mealType: MealType) {
        viewModelScope.launch {
            getSmartSuggestionsUseCase
                .getSuggestions(_uiState.value.selectedDate, mealType, 2000)
                .collect { suggestions ->
                    _uiState.update { it.copy(suggestions = suggestions) }
                }
        }
    }

    fun toggleView() = _uiState.update { it.copy(showWeekView = !it.showWeekView) }
    fun dismissError() = _uiState.update { it.copy(error = null) }
}