package io.github.innoobwetrust.kintamanga.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.notification.NotificationHandler
import io.github.innoobwetrust.kintamanga.notification.NotificationId
import io.github.innoobwetrust.kintamanga.notification.NotificationReceiver
import io.github.innoobwetrust.kintamanga.util.extension.chop
import io.github.innoobwetrust.kintamanga.util.extension.notificationManager
import java.util.regex.Pattern


/**
 * DownloadNotifier is used to show notifications when downloading one or multiple chapters.
 *
 * @param context context of application
 */
internal class DownloadNotifier(private val context: Context) {
    private val channelId by lazy {
        context.getString(R.string.app_name)
    }

    /**
     * Notification builder.
     */
    private val notification by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel: NotificationChannel? = context.notificationManager.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
                context.notificationManager.createNotificationChannel(notificationChannel!!)
            }
        }
        NotificationCompat.Builder(context, channelId)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
    }

    /**
     * Status of download. Used for correct notification icon.
     */
    private var isDownloading = false

    /**
     * The size of queue on start download.
     */
    var initialQueueSize = 0
        set(value) {
            if (value != 0) {
                isSingleChapter = (value == 1)
            }
            field = value
        }

    /**
     * Simultaneous download setting > 1.
     */
    var multipleDownloadThreads = false

    /**
     * Updated when error is thrown
     */
    var errorThrown = false

    /**
     * Updated when only single page is downloaded
     */
    var isSingleChapter = false

    /**
     * Updated when paused
     */
    var paused = false

    /**
     * Shows a notification from this builder.
     *
     * @param id the id of the notification.
     */
    private fun NotificationCompat.Builder.show(id: Int = NotificationId.DOWNLOAD_CHAPTER_ID) {
        context.notificationManager.notify(id, build())
    }

    /**
     * Dismiss the downloader's notification. Downloader error notifications use a different id, so
     * those can only be dismissed by the user.
     */
    fun dismiss() {
        context.notificationManager.cancel(NotificationId.DOWNLOAD_CHAPTER_ID)
    }

    /**
     * Called when download progress changes.
     * Note: Only accepted when multi download active.
     *
     * @param queue the queue containing downloads.
     */
    fun onProgressChange(queue: DownloadQueue) {
        if (multipleDownloadThreads) {
            doOnProgressChange(null, queue)
        }
    }

    /**
     * Called when download progress changes.
     * Note: Only accepted when single download active.
     *
     * @param download download object containing download information.
     * @param queue the queue containing downloads.
     */
    fun onProgressChange(download: Download, queue: DownloadQueue) {
        if (!multipleDownloadThreads) {
            doOnProgressChange(download, queue)
        }
    }

    /**
     * Show notification progress of chapter.
     *
     * @param download download object containing download information.
     * @param queue the queue containing downloads.
     */
    private fun doOnProgressChange(download: Download?, queue: DownloadQueue) {
        // Create notification
        with(notification) {
            // Check if first call.
            if (!isDownloading) {
                setSmallIcon(android.R.drawable.stat_sys_download)
                setAutoCancel(false)
                // Pause action
                addAction(
                        R.drawable.ic_notification_action_pause,
                        context.getString(R.string.action_pause),
                        NotificationReceiver.pauseDownloadsPendingBroadcast(context)
                )
                // Stop action
                addAction(
                        R.drawable.ic_notification_action_stop,
                        context.getString(R.string.action_stop),
                        NotificationReceiver.stopDownloadsPendingBroadcast(context)
                )
                //Clear action
                addAction(
                        R.drawable.ic_notification_action_delete_sweep,
                        context.getString(R.string.action_clear),
                        NotificationReceiver.clearDownloadsPendingBroadcast(context)
                )
                // Open download manager when clicked
                setContentIntent(NotificationHandler.openDownloadManagerPendingActivity(context))
                isDownloading = true
            }
            // Reset pause status
            paused = false

            if (multipleDownloadThreads) {
                setContentTitle(context.getString(R.string.app_name))

                // Reset the queue size if the download progress is negative
                if ((initialQueueSize - queue.size) < 0)
                    initialQueueSize = queue.size
                setContentText(context.getString(
                        R.string.chapter_downloading_progress,
                        initialQueueSize - queue.size,
                        initialQueueSize
                ))
                setProgress(initialQueueSize, initialQueueSize - queue.size, false)
            } else {
                download?.let {
                    val title = it.manga.mangaTitle.chop(15)
                    val quotedTitle = Pattern.quote(title)
                    val chapter = download.chapter.chapterTitle.replaceFirst("$quotedTitle[\\s]*[-]*[\\s]*".toRegex(RegexOption.IGNORE_CASE), "")
                    setContentTitle("$title - $chapter".chop(30))
                    setContentText(context.getString(
                            R.string.chapter_downloading_progress,
                            it.downloadedImages,
                            it.pages!!.size
                    ))
                    setProgress(it.pages!!.size, it.downloadedImages, false)

                }
            }
        }
        // Displays the progress bar on notification
        notification.show()
    }

    /**
     * Show notification when download is halted.
     */
    fun onDownloadHalted() {
        with(notification) {
            setContentTitle(context.getString(if (paused) R.string.chapter_paused else R.string.chapter_stopped))
            setContentText(context.getString(if (paused) R.string.download_notifier_download_paused else R.string.download_notifier_download_stopped))
            setSmallIcon(if (paused) R.drawable.ic_notification_action_pause else R.drawable.ic_notification_action_stop)
            setAutoCancel(false)
            setProgress(0, 0, false)
            // Open download manager when clicked
            setContentIntent(NotificationHandler.openDownloadManagerPendingActivity(context))
            // Resume action
            addAction(
                    R.drawable.ic_notification_action_play,
                    context.getString(R.string.action_resume),
                    NotificationReceiver.resumeDownloadsPendingBroadcast(context)
            )
            //Clear action
            addAction(
                    R.drawable.ic_notification_action_delete_sweep,
                    context.getString(R.string.action_clear),
                    NotificationReceiver.clearDownloadsPendingBroadcast(context)
            )
        }

        // Show notification.
        notification.show()

        // Reset initial values
        isDownloading = false
        initialQueueSize = 0
    }

    /**
     * Called when chapter is downloaded.
     *
     * @param download download object containing download information.
     */
    fun onDownloadCompleted(download: Download, queue: DownloadQueue) {
        // Check if last download
        if (!queue.isEmpty()) {
            return
        }
        // Create notification.
        with(notification) {
            val title = download.manga.mangaTitle.chop(15)
            val quotedTitle = Pattern.quote(title)
            val chapter = download.chapter.chapterTitle.replaceFirst("$quotedTitle[\\s]*[-]*[\\s]*".toRegex(RegexOption.IGNORE_CASE), "")
            setContentTitle("$title - $chapter".chop(30))
            setContentText(context.getString(R.string.download_notifier_download_complete))
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setAutoCancel(true)
            setContentIntent(NotificationReceiver.openChapterPendingBroadcast(context, download.manga, download.chapter))
            setProgress(0, 0, false)
        }

        // Show notification.
        notification.show()

        // Reset initial values
        isDownloading = false
        initialQueueSize = 0
    }

    /**
     * Called when the downloader receives a warning.
     *
     * @param reason the text to show.
     */
    fun onWarning(reason: String) {
        with(notification) {
            setContentTitle(context.getString(R.string.download_notifier_downloader_title))
            setContentText(reason)
            setSmallIcon(android.R.drawable.stat_sys_warning)
            setAutoCancel(true)
            setContentIntent(NotificationHandler.openDownloadManagerPendingActivity(context))
            setProgress(0, 0, false)
        }
        notification.show()

        // Reset download information
        isDownloading = false
    }

    /**
     * Called when the downloader receives an error. It's shown as a separate notification to avoid
     * being overwritten.
     *
     * @param error string containing error information.
     * @param chapter string containing chapter title.
     */
    fun onError(error: String? = null, chapter: String? = null) {
        // Create notification
        with(notification) {
            setContentTitle(chapter ?: context.getString(R.string.download_notifier_downloader_title))
            setContentText(error ?: context.getString(R.string.download_notifier_unknown_error))
            setSmallIcon(android.R.drawable.stat_sys_warning)
            setAutoCancel(false)
            setContentIntent(NotificationHandler.openDownloadManagerPendingActivity(context))
            setProgress(0, 0, false)
        }
        notification.show(NotificationId.DOWNLOAD_CHAPTER_ERROR_ID)

        // Reset download information
        errorThrown = true
        isDownloading = false
    }
}
