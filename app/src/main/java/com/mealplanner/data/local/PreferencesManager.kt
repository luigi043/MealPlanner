package com.mealplanner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mealplanner.domain.model.PlanningGoal
import com.mealplanner.domain.model.UserGoals
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val DAILY_CALORIE_TARGET = intPreferencesKey("daily_calorie_target")
        val DAILY_PROTEIN_TARGET = intPreferencesKey("daily_protein_target")
        val DAILY_CARBS_TARGET = intPreferencesKey("daily_carbs_target")
        val DAILY_FAT_TARGET = intPreferencesKey("daily_fat_target")
        val PLANNING_GOAL = stringPreferencesKey("planning_goal")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val userGoals: Flow<UserGoals> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserGoals(
                dailyCalorieTarget = preferences[PreferencesKeys.DAILY_CALORIE_TARGET] ?: 2000,
                dailyProteinTarget = preferences[PreferencesKeys.DAILY_PROTEIN_TARGET] ?: 150,
                dailyCarbsTarget = preferences[PreferencesKeys.DAILY_CARBS_TARGET] ?: 250,
                dailyFatTarget = preferences[PreferencesKeys.DAILY_FAT_TARGET] ?: 65,
                planningGoal = PlanningGoal.valueOf(
                    preferences[PreferencesKeys.PLANNING_GOAL] ?: PlanningGoal.BALANCED.name
                )
            )
        }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true }
    val darkModeEnabled: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.DARK_MODE_ENABLED] ?: false }

    suspend fun updateUserGoals(goals: UserGoals) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_CALORIE_TARGET] = goals.dailyCalorieTarget
            preferences[PreferencesKeys.DAILY_PROTEIN_TARGET] = goals.dailyProteinTarget
            preferences[PreferencesKeys.DAILY_CARBS_TARGET] = goals.dailyCarbsTarget
            preferences[PreferencesKeys.DAILY_FAT_TARGET] = goals.dailyFatTarget
            preferences[PreferencesKeys.PLANNING_GOAL] = goals.planningGoal.name
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_MODE_ENABLED] = enabled }
    }
}
