package io.github.innoobwetrust.kintamanga.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.github.innoobwetrust.kintamanga.ui.downloader.DownloaderActivity

/**
 * Class that manages [PendingIntent] of activity's
 */
object NotificationHandler {
    /**
     * Returns [PendingIntent] that starts a download activity.
     *
     * @param context context of application
     */
    internal fun openDownloadManagerPendingActivity(context: Context): PendingIntent {
        val intent = Intent(context, DownloaderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }
}