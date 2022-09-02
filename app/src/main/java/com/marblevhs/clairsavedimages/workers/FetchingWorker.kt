package com.marblevhs.clairsavedimages.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marblevhs.clairsavedimages.BuildConfig
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.monoRepo.Repo
import javax.inject.Inject

class FetchingWorker constructor(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params
) {

    @Inject
    lateinit var repo: Repo

    override suspend fun doWork(): Result {


        try {
            this.applicationContext.appComponent.inject(this)
            val newImagesAppeared: Boolean = repo.isThereNewImages()
            if (newImagesAppeared) {
                val resultIntent = Intent(applicationContext, MainActivity::class.java)
                val resultPendingIntent: PendingIntent? =
                    TaskStackBuilder.create(applicationContext).run {
                        addNextIntentWithParentStack(resultIntent)
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }
                val builder = NotificationCompat.Builder(applicationContext, "FetchingWorkerID")
                    .setSmallIcon(R.drawable.ic_baseline_favorite_36)
                    .setContentTitle("Новые картинки!")
                    .setContentText("Кажется в альбоме появились новые картинки. Зайди посмотри!")
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                val notif = builder.build()
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(R.string.new_image_notification_id, notif)
                }
            } else if (BuildConfig.DEBUG) {
                val debugNotif =
                    NotificationCompat.Builder(applicationContext, "FetchingWorkerID")
                        .setSmallIcon(R.drawable.ic_baseline_favorite_36)
                        .setContentTitle("Debug")
                        .setContentText("FetchingWorker сработал")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(2002, debugNotif)
                }
            }

        } catch (e: Exception) {
            return Result.retry()
        }


        return Result.success()
    }


}