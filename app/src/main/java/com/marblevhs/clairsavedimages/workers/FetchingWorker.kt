package com.marblevhs.clairsavedimages.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marblevhs.clairsavedimages.BuildConfig
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.R
import com.marblevhs.clairsavedimages.repositories.ImagesRepo
import com.marblevhs.clairsavedimages.repositories.SettingsRepo
import com.marblevhs.clairsavedimages.utils.appComponent
import javax.inject.Inject

class FetchingWorker constructor(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params
) {

    @Inject
    lateinit var imagesRepo: ImagesRepo

    @Inject
    lateinit var settingsRepo: SettingsRepo


    override suspend fun doWork(): Result {
        try {
            applicationContext.appComponent.inject(this)
            if (settingsRepo.isNotificationsEnabled()) {
                val newImagesAppeared: Boolean = imagesRepo.isThereNewImages()
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
                        notify(R.string.debug_fetching_worker_notification_id, debugNotif)
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("Fetching Worker", e.message.toString())
            return Result.retry()
        }

    }


}