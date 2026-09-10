package ph.gov.deped.region12.soxclmd.data

import android.content.Context
import androidx.work.*
import ph.gov.deped.region12.soxclmd.AppApplication
import java.util.concurrent.TimeUnit

/**
 * Background synchronization driven by WorkManager:
 *  - a periodic job every 6 hours,
 *  - an expedited one-shot job whenever connectivity returns.
 *
 * Both are best-effort; the UI also triggers a sync on app open and on
 * return from background, so users almost always see fresh content.
 */
class SyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = (applicationContext as? AppApplication)?.repository
            ?: return Result.failure()
        return try {
            repo.sync()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val PERIODIC = "soxclmd_periodic_sync"
        private const val ONESHOT = "soxclmd_connectivity_sync"

        fun schedule(context: Context) {
            val wm = WorkManager.getInstance(context)
            val connected = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            wm.enqueueUniquePeriodicWork(
                PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                    .setConstraints(connected)
                    .build()
            )

            wm.enqueueUniqueWork(
                ONESHOT,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(connected)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            )
        }
    }
}
