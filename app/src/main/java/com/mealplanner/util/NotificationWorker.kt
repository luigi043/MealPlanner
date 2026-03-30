package com.mealplanner.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mealplanner.data.repository.MealPlanRepository
import com.mealplanner.data.local.PreferencesManager
import com.mealplanner.domain.model.MealType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val mealPlanRepository: MealPlanRepository,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = preferencesManager.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()

        val today = LocalDate.now()
        val meals = mealPlanRepository.getMealPlansByDate(today).first()

        if (meals.isNotEmpty()) {
            // Logic to determine which meal to notify about based on current time
            // For now, we'll send a general summary if there are meals planned
            val mealSummary = meals.joinToString(", ") { it.recipeName }
            NotificationHelper.showMealReminder(
                applicationContext,
                "Today's Plan 🍽️",
                "You have planned: $mealSummary"
            )
        }

        return Result.success()
    }
}
