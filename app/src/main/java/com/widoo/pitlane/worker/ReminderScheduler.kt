package com.widoo.pitlane.worker

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(
        context: Context,
        notificationId: Int,
        title: String,
        description: String,
        triggerDateMillis: Long
    ) {
        val delay = triggerDateMillis - System.currentTimeMillis()

        // Don't schedule if date is in the past
        if (delay <= 0) return

        val inputData = workDataOf(
            ReminderWorker.KEY_TITLE to title,
            ReminderWorker.KEY_DESCRIPTION to description,
            ReminderWorker.KEY_NOTIFICATION_ID to notificationId
        )

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_$notificationId")
            .build()

        WorkManager.getInstance(context)
            .enqueue(workRequest)
    }

    fun cancel(context: Context, notificationId: Int) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("reminder_$notificationId")
    }
}