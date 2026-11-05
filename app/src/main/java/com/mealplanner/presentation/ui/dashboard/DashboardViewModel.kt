package com.mealplanner.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealplanner.data.repository.MealPlanRepository
import com.mealplanner.data.repository.RecipeRepository
import com.mealplanner.domain.model.*
import com.mealplanner.domain.usecase.GetNutritionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.mealplanner.data.local.PreferencesManager
import kotlinx.coroutines.Job

data class DashboardUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val todayMeals: List<MealPlan> = emptyList(),
    val nutritionSummary: NutritionSummary? = null,
    val weeklySummary: WeeklyNutritionSummary? = null,
    val recentRecipes: List<Recipe> = emptyList(),
    val userGoals: UserGoals = UserGoals(),
    val streakDays: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val getNutritionSummaryUseCase: GetNutritionSummaryUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val today = LocalDate.now()
    private var dashboardJob: Job? = null

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            combine(
                mealPlanRepository.getMealPlansByDate(today),
                getNutritionSummaryUseCase.getDailySummary(today),
                getNutritionSummaryUseCase.getWeeklySummary(today.minusDays(today.dayOfWeek.value.toLong() - 1)),
                recipeRepository.getAllRecipes(),
                preferencesManager.userGoals
            ) { todayMeals, dailyNutrition, weeklySummary, recipes, goals ->
                DashboardUiState(
                    isLoading = false,
                    today = today,
                    todayMeals = todayMeals,
                    nutritionSummary = dailyNutrition,
                    weeklySummary = weeklySummary,
                    recentRecipes = recipes.take(5),
                    userGoals = goals,
                    streakDays = calculateStreak(weeklySummary),
                    error = null
                )
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateStreak(weekly: WeeklyNutritionSummary): Int =
        weekly.dailySummaries.filter { !it.date.isAfter(today) }.takeLastWhile { it.mealCount > 0 }.size

    fun dismissError() = _uiState.update { it.copy(error = null) }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadDashboard()
    }
}
