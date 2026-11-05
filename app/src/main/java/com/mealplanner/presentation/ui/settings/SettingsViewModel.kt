package com.mealplanner.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealplanner.data.local.PreferencesManager
import com.mealplanner.domain.model.PlanningGoal
import com.mealplanner.domain.model.UserGoals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userGoals: UserGoals = UserGoals(),
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.userGoals,
        preferencesManager.notificationsEnabled,
        preferencesManager.darkModeEnabled
    ) { goals, notifications, darkMode ->
        SettingsUiState(
            userGoals = goals,
            notificationsEnabled = notifications,
            darkModeEnabled = darkMode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateCalorieTarget(target: String) {
        val value = target.toIntOrNull()?.takeIf { it in 1..100000 } ?: return
        viewModelScope.launch {
            preferencesManager.updateUserGoals(uiState.value.userGoals.copy(dailyCalorieTarget = value))
        }
    }

    fun updateProteinTarget(target: String) {
        val value = target.toIntOrNull()?.takeIf { it in 0..100000 } ?: return
        viewModelScope.launch {
            preferencesManager.updateUserGoals(uiState.value.userGoals.copy(dailyProteinTarget = value))
        }
    }

    fun updateCarbsTarget(target: String) {
        val value = target.toIntOrNull()?.takeIf { it in 0..100000 } ?: return
        viewModelScope.launch {
            preferencesManager.updateUserGoals(uiState.value.userGoals.copy(dailyCarbsTarget = value))
        }
    }

    fun updateFatTarget(target: String) {
        val value = target.toIntOrNull()?.takeIf { it in 0..100000 } ?: return
        viewModelScope.launch {
            preferencesManager.updateUserGoals(uiState.value.userGoals.copy(dailyFatTarget = value))
        }
    }

    fun updatePlanningGoal(goal: PlanningGoal) {
        viewModelScope.launch {
            preferencesManager.updateUserGoals(uiState.value.userGoals.copy(planningGoal = goal))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateNotificationsEnabled(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateDarkModeEnabled(enabled)
        }
    }
}
