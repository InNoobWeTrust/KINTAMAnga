package io.github.innoobwetrust.kintamanga.ui.manga

import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.pushtorefresh.storio.sqlite.operations.get.PreparedGetListOfObjects
import com.pushtorefresh.storio.sqlite.operations.get.PreparedGetObject
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.download.DownloadProvider
import io.github.innoobwetrust.kintamanga.download.DownloadProvider.markDownloadedChapters
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.ui.reader.ViewerTypes
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

interface MangaInfoLoader : KodeinGlobalAware {
    val mangaInfoProcessor: MangaInfoProcessor
    var mangaBinding: MangaBinding

    var refreshDisposable: Subscription?
    var observeDownloaderDisposable: Subscription?

    private val databaseHelper: DatabaseHelper
        get() = instance()

    private fun getMangaDb(): PreparedGetObject<MangaDb> =
            databaseHelper.getManga(mangaBinding.mangaUri, mangaBinding.mangaSourceName)

    private fun putMangaDb(): Boolean =
            databaseHelper
                    .insertManga(mangaBinding)
                    .executeAsBlocking()
                    .run { wasInserted() || wasUpdated() }
                    .also {
                        getMangaDb().executeAsBlocking()
                                ?.copyTo(mangaBinding)
                    }

    private fun deleteMangaDb(): Boolean =
            if (null == mangaBinding.id)
                true
            else
                databaseHelper
                        .deleteManga(mangaBinding)
                        .executeAsBlocking()
                        .run { 0 < numberOfRowsDeleted() }
                        .also {
                            if (it) mangaBinding.id = null
                        }

    private fun saveManga(): Observable<Boolean> = Observable
            .fromCallable { putMangaDb() }
            .doOnError {
                Timber.e(it)
            }
            .subscribeOn(Schedulers.io())

    private fun removeOrUpdateMangaDb(): Observable<Boolean> =
            Observable
                    .fromCallable {
                        if (!mangaBinding.mangaFavorited && !mangaBinding.mangaDownloaded)
                            deleteMangaDb()
                        else
                            putMangaDb()
                    }
                    .doOnError {
                        Timber.e(it)
                    }
                    .subscribeOn(Schedulers.io())

    private fun getChapterDbs(): PreparedGetListOfObjects<ChapterDb> =
            databaseHelper.getChapters(mangaBinding)

    @Throws(Exception::class)
    private fun updateMangaChapterDbs(): Boolean {
        if (null == mangaBinding.id) return true
        var chapterDbs = getChapterDbs().executeAsBlocking()
        val newIdList = mangaBinding.chapters.mapNotNull { it.id }
        // Delete chapters which are removed from online sources
        val deleteResults = databaseHelper.deleteChapters(
                chapterDbs.filter { it.id !in newIdList }
        ).executeAsBlocking().results()
        // Return false if any deletion failed
        if (deleteResults.values.any { 0 == it.numberOfRowsDeleted() }) {
            return false
        }
        // Update list of chapters
        Timber.v("Number of chapters to put to database: ${mangaBinding.chapters.size}")
        val putResults = databaseHelper
                .insertChapters(mangaBinding.chapters)
                .executeAsBlocking()
                .results()
        // Return false if any insertion failed
        if (putResults.values.any { it.wasNotInserted() && it.wasNotUpdated() }) {
            return false
        }
        // Update Id of chapters
        chapterDbs = getChapterDbs().executeAsBlocking()
        Timber.v("Number of chapters in database: ${chapterDbs.size}")
        mangaBinding.chapters.forEach { chapterBinding ->
            chapterDbs.find { it.chapterIndex == chapterBinding.chapterIndex }
                    ?.copyTo(chapterBinding)
        }
        return true
    }

    @Throws(Exception::class)
    private fun updateChapterDbs(chapterDbs: List<ChapterDb>): Boolean {
        if (null == mangaBinding.id) return true
        if (chapterDbs.any { null == it.id }) return true
        val putResults = databaseHelper
                .insertChapters(chapterDbs)
                .executeAsBlocking()
                .results()
        // Return false if any insertion failed
        if (putResults.values.any { it.wasNotInserted() && it.wasNotUpdated() }) {
            return false
        }
        return true
    }

    private fun saveChapters(chapters: List<ChapterDb>? = null): Observable<Boolean> =
            Observable
                    .fromCallable {
                        chapters?.let {
                            updateChapterDbs(it)
                        } ?: updateMangaChapterDbs()
                    }
                    .doOnError {
                        Timber.e(it)
                    }
                    .subscribeOn(Schedulers.io())

    private fun saveFullInfo(): Observable<Boolean> =
            saveManga()
                    .flatMap { saveChapters() }
                    .doOnError {
                        Timber.e(it)
                    }
                    .subscribeOn(Schedulers.io())

    @Throws(Exception::class)
    private fun localLoadMangaInfo(): Boolean = getMangaDb().executeAsBlocking()?.let {
        it.copyTo(mangaDb = mangaBinding)
        mangaBinding.chapters = getChapterDbs().executeAsBlocking()
                .map { chapterDb -> chapterDb.asChapterBinding() }
                .sortedBy { chapterDb -> chapterDb.chapterIndex }
        true
    } == true

    @Throws(Exception::class)
    private fun networkLoadMangaInfo(): Boolean {
        if (!mangaInfoProcessor.fetchFullInfo(mangaBinding = mangaBinding)) return false
        // Fix any out of range viewer type from old database
        if (!ViewerTypes.values().map { it.ordinal }.contains(mangaBinding.mangaViewer))
            mangaBinding.mangaViewer = 0
        return true
    }

    @Throws(Exception::class)
    private fun loadMangaInfo(): Boolean = localLoadMangaInfo().run { networkLoadMangaInfo() }

    private fun refresh(): Observable<Boolean> = Observable
            .fromCallable { loadMangaInfo() }
            .doOnNext { success ->
                markDownloadedChapters(mangaBinding = mangaBinding)
                if (success) {
                    if (null != mangaBinding.id || mangaBinding.mangaDownloaded) {
                        putMangaDb()
                        updateMangaChapterDbs()
                    }
                }
            }
            .doOnError {
                Timber.e(it)
            }
            .subscribeOn(Schedulers.io())

    fun backgroundRefresh(
            onRefreshSuccess: (Boolean) -> Unit,
            onError: (Throwable) -> Unit
    ) {
        refreshDisposable = refresh()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success -> onRefreshSuccess(success) },
                        { error -> onError(error) }
                )
    }

    fun backgroundSaveChapters(chapterBindings: List<ChapterBinding>? = null): Subscription =
            saveChapters(chapterBindings).subscribe()

    fun backgroundRemoveOrUpdateMangaDb(): Subscription = removeOrUpdateMangaDb().subscribe()

    fun backgroundSaveFullInfo(): Subscription = saveFullInfo().subscribe()

    fun observeDownloads(
            onDownloadStatusChange: (Download) -> Unit,
            onError: ((Throwable) -> Unit)?
    ) {
        observeDownloaderDisposable = instance<Downloader>()
                .queue
                .getStatusObservable()
                .filter { mangaBinding.id == it.manga.id }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Timber.e(it)
                }
                .subscribe(
                        { download -> onDownloadStatusChange(download) },
                        { error -> onError?.invoke(error) }
                )
    }

    fun backgroundPrepareForDownload(
            chapterBindings: List<ChapterBinding>,
            onSuccess: (Boolean) -> Unit,
            onError: (Throwable) -> Unit
    ) {
        val saveMangaObservable: Observable<Boolean> =
                if (null == mangaBinding.id || mangaBinding.chapters.any { null == it.id })
                    saveFullInfo()
                else
                    Observable.just(true)
        saveMangaObservable
                .doOnError {
                    Timber.e(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { success -> onSuccess(success) },
                        { error -> onError(error) }
                )
    }

    fun backgroundRemoveDownloadedChapters(
            chapterBindings: List<ChapterBinding>,
            onComplete: (() -> Unit)?,
            onError: ((Throwable) -> Unit)?
    ) {
        Observable
                .fromCallable {
                    chapterBindings.forEach { chapterBinding ->
                        DownloadProvider.getChapterDir(
                                mangaBinding.mangaSourceName,
                                mangaBinding.mangaTitle,
                                chapterBinding.chapterTitle
                        )?.let {
                            it.deleteRecursively()
                            chapterBinding.chapterDownloadStatus = DownloadStatus.NOT_DOWNLOADED
                        }
                    }
                    if (mangaBinding.chapters
                            .filter {
                                it.chapterDownloadStatus == DownloadStatus.NOT_DOWNLOADED
                            }
                            .size == mangaBinding.chapters.size) {
                        mangaBinding.mangaDownloaded = false
                        backgroundRemoveOrUpdateMangaDb()
                    }
                }
                .doOnError {
                    Timber.e(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onComplete?.invoke() }, { onError?.invoke(it) })
    }

    fun backgroundRemoveAllDownloads(
            onComplete: () -> Unit,
            onError: ((Throwable) -> Unit)?
    ) {
        Observable
                .fromCallable {
                    instance<Downloader>().let { downloader ->
                        downloader.queue
                                .filter { mangaBinding.id == it.manga.id }
                                .forEach { downloader.remove(it) }
                    }
                    DownloadProvider.getMangaDir(
                            sourceName = mangaBinding.mangaSourceName,
                            mangaTitle = mangaBinding.mangaTitle
                    )?.deleteRecursively()
                    mangaBinding.chapters
                            .forEach { it.chapterDownloadStatus = DownloadStatus.NOT_DOWNLOADED }
                    mangaBinding.mangaDownloaded = false
                    backgroundRemoveOrUpdateMangaDb()
                }
                .doOnError {
                    Timber.e(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onComplete() }, { onError?.invoke(it) })
    }

    fun disposeAllLoaderDisposables() {
        refreshDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
        observeDownloaderDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }
}
