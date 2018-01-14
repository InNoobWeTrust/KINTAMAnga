package io.github.innoobwetrust.kintamanga.download

import android.content.SharedPreferences
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.Gson
import io.github.innoobwetrust.kintamanga.KINTAMAngaPreferences
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.model.Download

// Borrow heavily from Tachiyomi app (https://github.com/inorichi/tachiyomi)
object DownloadStore : KodeinGlobalAware {
    /**
     * Preference file where active downloads are stored.
     */
    private val preferences: SharedPreferences = instance(KINTAMAngaPreferences.ACTIVE_DOWNLOADS.key)

    /**
     * Counter used to keep the queue order.
     */
    private var counter = 0

    /**
     * Adds a list of downloads to the store.
     *
     * @param downloads the list of downloads to add.
     */
    fun addAll(downloads: List<Download>) {
        val editor = preferences.edit()
        downloads.forEach { editor.putString(getKey(it), serialize(it)) }
        editor.apply()
    }

    /**
     * Removes a download from the store.
     *
     * @param download the download to remove.
     */
    fun remove(download: Download) {
        preferences.edit().remove(getKey(download)).apply()
    }

    /**
     * Removes all the downloads from the store.
     */
    fun clear() {
        preferences.edit().clear().apply()
    }

    /**
     * Returns the preference's key for the given download.
     *
     * @param download the download.
     */
    private fun getKey(download: Download): String {
        return download.chapter.id!!.toString()
    }

    /**
     * Returns the list of downloads to restore. It should be called in a background thread.
     */
    fun restore(): List<Download> {
        val objs = preferences.all
                .mapNotNull { it.value as? String }
                .map { deserialize(it) }
                .sortedBy { it.order }

        val downloads = mutableListOf<Download>()
        if (objs.isNotEmpty()) {
            val cachedManga = mutableMapOf<Long, MangaDb?>()
            for ((mangaId, chapterId) in objs) {
                val manga = cachedManga.getOrPut(mangaId) {
                    instance<DatabaseHelper>().getManga(mangaId).executeAsBlocking()
                } ?: continue
                val chapter = instance<DatabaseHelper>()
                        .getChapter(chapterId)
                        .executeAsBlocking() ?: continue
                downloads.add(Download(manga, chapter))
            }
        }
        // Clear the store, downloads will be added again immediately.
        clear()
        return downloads
    }

    /**
     * Converts a download to a string.
     *
     * @param download the download to serialize.
     */
    private fun serialize(download: Download): String {
        val obj = DownloadObject(download.manga.id!!, download.chapter.id!!, counter++)
        return instance<Gson>().typedToJson(obj)
    }

    /**
     * Restore a download from a string.
     *
     * @param string the download as string.
     */
    private fun deserialize(string: String): DownloadObject =
            instance<Gson>().fromJson(string)

    /**
     * Class used for download serialization
     *
     * @param mangaId the id of the manga.
     * @param chapterId the id of the chapter.
     * @param order the order of the download in the queue.
     */
    data class DownloadObject(val mangaId: Long, val chapterId: Long, val order: Int)
}