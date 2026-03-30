package com.mealplanner.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

const val MEAL_REMINDER_CHANNEL_ID = "meal_reminder_channel"
const val MEAL_REMINDER_NOTIFICATION_ID = 1001

class MealReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mealType = intent.getStringExtra("meal_type") ?: "Meal"
        showMealReminderNotification(context, mealType)
    }

    private fun showMealReminderNotification(context: Context, mealType: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEAL_REMINDER_CHANNEL_ID,
                "Meal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for planned meal times"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, MEAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for $mealType! 🍽️")
            .setContentText("You have meals planned. Check your meal plan!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(MEAL_REMINDER_NOTIFICATION_ID, notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule meal reminders after device reboot
        }
    }
}