package io.github.innoobwetrust.kintamanga.download

import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.model.Page
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.CopyOnWriteArrayList

// Borrow heavily from Tachiyomi app (https://github.com/inorichi/tachiyomi)
class DownloadQueue(
        private val queue: MutableList<Download> = CopyOnWriteArrayList()
) : List<Download> by queue {

    private val statusSubject = PublishSubject.create<Download>()

    private val updatedSubject = PublishSubject.create<Unit>()

    fun addAll(downloads: List<Download>) {
        downloads.forEach { download ->
            download.setStatusSubject(statusSubject)
            download.downloadStatus = DownloadStatus.QUEUE
        }
        queue.addAll(downloads)
        DownloadStore.addAll(downloads)
        updatedSubject.onNext(Unit)
    }

    fun remove(download: Download) {
        val removed = queue.remove(download)
        DownloadStore.remove(download)
        DownloadProvider.getTempChapterDir(
                sourceName = download.manga.mangaSourceName,
                mangaTitle = download.manga.mangaTitle,
                chapterTitle = download.chapter.chapterTitle
        )?.deleteRecursively()
        download.setStatusSubject(null)
        if (removed) {
            updatedSubject.onNext(Unit)
        }
    }

    fun remove(chapter: ChapterDb) {
        find { it.chapter.id == chapter.id }?.let { remove(it) }
    }

    fun clear() {
        queue.forEach { download ->
            download.setStatusSubject(null)
            DownloadProvider.getTempChapterDir(
                    sourceName = download.manga.mangaSourceName,
                    mangaTitle = download.manga.mangaTitle,
                    chapterTitle = download.chapter.chapterTitle
            )?.deleteRecursively()
        }
        queue.clear()
        DownloadStore.clear()
        updatedSubject.onNext(Unit)
    }

    private fun getActiveDownloads(): Observable<Download> =
            Observable.from(this).filter { download -> download.downloadStatus == DownloadStatus.DOWNLOADING }

    fun getStatusObservable(): Observable<Download> = statusSubject.onBackpressureBuffer()

    fun getUpdatedObservable(): Observable<List<Download>> = updatedSubject.onBackpressureBuffer()
            .startWith(Unit)
            .map { this }

    private fun setPagesSubject(pages: List<Page>?, subject: PublishSubject<DownloadStatus>?) =
            pages?.forEach { it.setStatusSubject(subject) }
}
