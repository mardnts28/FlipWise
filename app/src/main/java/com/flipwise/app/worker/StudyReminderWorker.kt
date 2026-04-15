package com.flipwise.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flipwise.app.util.NotificationHelper
import com.flipwise.app.data.repository.FlipWiseRepository
import com.flipwise.app.data.database.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class StudyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = FlipWiseRepository(applicationContext)
        
        // Logic to check if user has studied today
        val sessions = db.studySessionDao().getAllSessions(repository.userId).firstOrNull() ?: emptyList()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val studiedToday = sessions.any { it.date >= today }
        
        if (!studiedToday) {
            NotificationHelper.showNotification(
                applicationContext,
                "Don't Lose Your Streak! 🔥",
                "You haven't finished your goal today. Keep studying to earn badges!"
            )
        } else {
            NotificationHelper.showNotification(
                applicationContext,
                "Ready for more? 📚",
                "Great job studying today! Want to review one more deck to lock in that knowledge?"
            )
        }

        return Result.success()
    }
}
