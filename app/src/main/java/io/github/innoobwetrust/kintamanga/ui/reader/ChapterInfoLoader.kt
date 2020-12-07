package io.github.innoobwetrust.kintamanga.ui.reader

import android.content.Context
import io.github.innoobwetrust.kintamanga.download.DownloadProvider
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

interface ChapterInfoLoader {
    val chapterInfoProcessor: ChapterInfoProcessor

    var mangaBinding: MangaBinding
    var chapterIndex: Int
    val chapterBinding: ChapterBinding
        get() = mangaBinding.chapters[chapterIndex]

    var loadChapterDisposable: Subscription?

    @Throws(Exception::class)
    private fun networkLoadChapterInfo(): Boolean {
        val pages = try {
            chapterInfoProcessor.fetchPageList(chapterBinding)
        } catch (e: Exception) {
            Timber.e(e)
            return false
        }
        chapterBinding.chapterPages = pages.onEach { it.chapterIndex = chapterBinding.chapterIndex }
        return true
    }

    @Throws(Exception::class)
    fun loadChapterInfo(context: Context): Boolean {
        if (DownloadStatus.DOWNLOADED == chapterBinding.chapterDownloadStatus)
            DownloadProvider.injectDownloadedChapterImages(
                    mangaBinding = mangaBinding,
                    chapterIndex = chapterIndex
            )
        return if (chapterBinding.chapterPages.isEmpty())
            networkLoadChapterInfo()
        else
            true
    }

    fun backgroundRefresh(
            context: Context,
            onChapterLoaded: (Boolean) -> Unit,
            onChapterLoadError: (Throwable) -> Unit
    ) {
        // Prevent conflict
        disposeAllLoaderDisposables()
        // Real job
        loadChapterDisposable = Single.fromCallable {
            loadChapterInfo(context = context)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success -> onChapterLoaded(success) },
                        { error -> onChapterLoadError(error) }
                )
    }

    fun disposeAllLoaderDisposables() {
        loadChapterDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }
}
