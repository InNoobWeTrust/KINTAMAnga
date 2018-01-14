package io.github.innoobwetrust.kintamanga.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.BuildConfig
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.service.DownloadService
import io.github.innoobwetrust.kintamanga.ui.manga.MangaInfoActivity
import io.github.innoobwetrust.kintamanga.ui.reader.ReaderActivity
import io.github.innoobwetrust.kintamanga.util.extension.notificationManager
import io.github.innoobwetrust.kintamanga.util.extension.toast

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver(), KodeinGlobalAware {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
        // Dismiss notification
            ACTION_DISMISS_NOTIFICATION -> dismissNotification(
                    context,
                    intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            )
        // Resume the download service
            ACTION_RESUME_DOWNLOADS -> {
                DownloadService.start(context)
                instance<Downloader>().let {
                    if (it.notifier.paused) it.resume()
                }
            }
        // Pause the downloader
            ACTION_PAUSE_DOWNLOADS -> instance<Downloader>().pause()
        // Stop the download service
            ACTION_STOP_DOWNLOADS -> DownloadService.stop(context)
        // Clear the download queue
            ACTION_CLEAR_DOWNLOADS -> instance<Downloader>().clearQueue(true)
        // Open reader activity
            ACTION_OPEN_CHAPTER -> {
                openChapter(context, intent.getLongExtra(EXTRA_MANGA_ID, -1),
                        intent.getLongExtra(EXTRA_CHAPTER_ID, -1))
            }
        }
    }

    /**
     * Dismiss the notification
     *
     * @param notificationId the id of the notification
     */
    private fun dismissNotification(context: Context, notificationId: Int) {
        context.notificationManager.cancel(notificationId)
    }

    /**
     * Starts reader activity
     *
     * @param context context of application
     * @param mangaId id of manga
     * @param chapterId id of chapter
     */
    private fun openChapter(context: Context, mangaId: Long, chapterId: Long) {
        val db = DatabaseHelper(context)
        val manga = db.getManga(mangaId).executeAsBlocking()
                ?: return Unit.also { context.toast(R.string.chapter_error) }
        val chapters = db.getChapters(manga).executeAsBlocking()
        val chapter = chapters.find { it.id == chapterId }
        if (chapter != null) {
            val intent = Intent(context, ReaderActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(
                            MangaInfoActivity.Companion.Intents.MANGA_INFO.key,
                            manga.asMangaBinding()
                                    .also { it.chapters = chapters.map { chapter -> chapter.asChapterBinding() } }
                    )
                    .putExtra(
                            MangaInfoActivity.Companion.Intents.CHAPTER_INDEX.key,
                            chapter.asChapterBinding().chapterIndex
                    )
            context.startActivity(intent)
        } else {
            context.toast(R.string.chapter_error)
        }
    }

    companion object {
        private const val NAME = "NotificationReceiver"

        // Called to open chapter
        private const val ACTION_OPEN_CHAPTER = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_OPEN_CHAPTER"

        // Called to resume downloads.
        private const val ACTION_RESUME_DOWNLOADS = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_RESUME_DOWNLOADS"

        // Called to pause downloads.
        private const val ACTION_PAUSE_DOWNLOADS = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_PAUSE_DOWNLOADS"

        // Called to stop downloads.
        private const val ACTION_STOP_DOWNLOADS = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_STOP_DOWNLOADS"

        // Called to clear downloads.
        private const val ACTION_CLEAR_DOWNLOADS = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_CLEAR_DOWNLOADS"

        // Called to dismiss notification.
        private const val ACTION_DISMISS_NOTIFICATION = "${BuildConfig.APPLICATION_ID}.$NAME.ACTION_DISMISS_NOTIFICATION"

        // Value containing notification id.
        private const val EXTRA_NOTIFICATION_ID = "${BuildConfig.APPLICATION_ID}.$NAME.NOTIFICATION_ID"

        // Value containing manga id.
        private const val EXTRA_MANGA_ID = "${BuildConfig.APPLICATION_ID}.$NAME.EXTRA_MANGA_ID"

        // Value containing chapter id.
        private const val EXTRA_CHAPTER_ID = "${BuildConfig.APPLICATION_ID}.$NAME.EXTRA_CHAPTER_ID"

        /**
         * Returns a [PendingIntent] that resumes the download of a chapter
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun resumeDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_RESUME_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns a [PendingIntent] that pauses the download of a chapter
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun pauseDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_PAUSE_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns a [PendingIntent] that stops the download of a chapter
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun stopDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_STOP_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns a [PendingIntent] that clears the download queue
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun clearDownloadsPendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_CLEAR_DOWNLOADS
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        /**
         * Returns [PendingIntent] that start a reader activity containing chapter.
         *
         * @param context context of application
         * @param manga manga of chapter
         * @param chapter chapter that needs to be opened
         */
        internal fun openChapterPendingBroadcast(
                context: Context,
                manga: MangaDb,
                chapter: ChapterDb
        ): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_OPEN_CHAPTER
                putExtra(EXTRA_MANGA_ID, manga.id)
                putExtra(EXTRA_CHAPTER_ID, chapter.id)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
