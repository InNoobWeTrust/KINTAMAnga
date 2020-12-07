package io.github.innoobwetrust.kintamanga.download

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.service.DownloadService
import io.github.innoobwetrust.kintamanga.source.SourceManager
import io.github.innoobwetrust.kintamanga.util.ImageConverter
import io.github.innoobwetrust.kintamanga.util.RetryWithDelay
import io.github.innoobwetrust.kintamanga.util.Storage
import io.github.innoobwetrust.kintamanga.util.extension.*
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.io.File

// Borrow heavily from Tachiyomi app (https://github.com/inorichi/tachiyomi)
/**
 * This class is the one in charge of downloading chapters.
 *
 * Its [queue] contains the list of chapters to download. In order to download them, the downloader
 * subscriptions must be running and the list of chapters must be sent to them by [downloadsSubject].
 *
 * The queue manipulation must be done in one thread (currently the main thread) to avoid unexpected
 * behavior, but it's safe to read it from multiple threads.
 *
 * @param context the application context.
 */
class Downloader(private val context: Context) : KodeinGlobalAware {
    /**
     * Queue where active downloads are kept.
     */
    val queue = DownloadQueue()

    /**
     * Notifier for the downloader state and progress.
     */
    internal val notifier by lazy { DownloadNotifier(context) }

    /**
     * Downloader subscriptions.
     */
    private val subscriptions = CompositeSubscription()

    /**
     * Subject to send a list of downloads to the downloader.
     */
    private val downloadsSubject = PublishSubject.create<List<Download>>()

    /**
     * Subject to subscribe to the downloader status.
     */
    val runningSubject: BehaviorSubject<Boolean> = BehaviorSubject.create(false)

    /**
     * Whether the downloader is running.
     */
    @Volatile
    var isRunning: Boolean = false
        private set

    init {
        Observable.fromCallable { DownloadStore.restore() }
                .map { downloads -> downloads.filter { isDownloadAllowed(it) } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { downloads ->
                            queue.addAll(downloads)
                        },
                        { error ->
                            Timber.e(error)
                        })
    }

    /**
     * Starts the downloader. It doesn't do anything if it's already running or there isn't anything
     * to download.
     *
     * @return true if the downloader is started, false otherwise.
     */
    fun start(): Boolean {
        if (isRunning || queue.isEmpty()) return false

        if (!subscriptions.hasSubscriptions())
            initializeSubscriptions()

        val pending = queue.filter { it.downloadStatus != DownloadStatus.DOWNLOADED }
        pending.forEach { if (it.downloadStatus != DownloadStatus.QUEUE) it.downloadStatus = DownloadStatus.QUEUE }

        downloadsSubject.onNext(pending)
        if (pending.isNotEmpty()) notifier.onProgressChange(queue) else notifier.dismiss()
        return pending.isNotEmpty()
    }

    /**
     * Prepares the subscriptions to start downloading.
     *
     */
    private fun initializeSubscriptions() {
        if (isRunning) return
        isRunning = true
        runningSubject.onNext(true)

        subscriptions.clear()

        notifier.multipleDownloadThreads = true

        subscriptions.add(downloadsSubject.flatMap { Observable.from(it) }
                .parallelMap { downloadChapter(it) }
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { completeDownload(it) },
                        { error ->
                            DownloadService.stop(context)
                            Timber.e(error)
                            notifier.onError(error.message)
                        }
                )
        )
    }

    /**
     * Resumes the downloader. It doesn't do anything if it's already running or there isn't anything
     * to download.
     *
     * @return true if the downloader is started, false otherwise.
     */
    fun resume(): Boolean {
        if (isRunning || queue.isEmpty()) return false

        if (!subscriptions.hasSubscriptions())
            initializeSubscriptions()

        val pending = queue.filter { it.downloadStatus !in listOf(DownloadStatus.DOWNLOADED, DownloadStatus.STOPPED) }
        pending.forEach { if (it.downloadStatus != DownloadStatus.QUEUE) it.downloadStatus = DownloadStatus.QUEUE }

        downloadsSubject.onNext(pending)
        notifier.paused = false
        if (pending.isNotEmpty()) notifier.onProgressChange(queue) else notifier.dismiss()
        return pending.isNotEmpty()
    }

    /**
     * Pauses the downloader
     */
    fun pause() {
        destroySubscriptions()
        queue
                .filter { it.downloadStatus == DownloadStatus.DOWNLOADING }
                .forEach { it.downloadStatus = DownloadStatus.QUEUE }
        notifier.paused = true
        notifier.onDownloadHalted()
    }

    /**
     * Stops the downloader.
     */
    fun stop(reason: String? = null) {
        destroySubscriptions()
        queue
                .filter { it.downloadStatus == DownloadStatus.DOWNLOADING }
                .forEach { it.downloadStatus = DownloadStatus.STOPPED }
        if (reason != null) {
            notifier.onWarning(reason)
        } else {
            if (notifier.paused) {
                notifier.paused = false
                notifier.onDownloadHalted()
            } else if (notifier.isSingleChapter && !notifier.errorThrown) {
                notifier.isSingleChapter = false
            } else {
                notifier.dismiss()
            }
        }
    }

    /**
     * Removes everything from the queue.
     *
     * @param isNotification value that determines if status is set (needed for view updates)
     */
    fun clearQueue(isNotification: Boolean = false) {
        destroySubscriptions()

        //Needed to update the chapter view
        if (isNotification) {
            queue
                    .filter { it.downloadStatus == DownloadStatus.QUEUE }
                    .forEach { it.downloadStatus = DownloadStatus.NOT_DOWNLOADED }
        }
        queue.clear()
        notifier.dismiss()
    }

    /**
     * Creates a download object for every chapter and adds them to the downloads queue. This method
     * must be called in the main thread.
     *
     * @param manga the manga of the chapters to download.
     * @param chapters the list of chapters to download.
     */
    fun queueChapters(manga: MangaDb, chapters: List<ChapterDb>) {
        val chaptersToQueue = chapters
                // Avoid downloading chapters with the same title.
                .distinctBy { it.chapterTitle }
                // Add chapters to queue from the start.
                .sortedBy { it.mangaId }
                // Create a downloader for each one.
                .map { Download(manga, it) }
                // Filter out those already queued or downloaded.
                .filter { isDownloadAllowed(it) }

        // Return if there's nothing to queue.
        if (chaptersToQueue.isEmpty()) return

        queue.addAll(chaptersToQueue)

        // Initialize queue size.
        notifier.initialQueueSize = queue.size

        // Initial multi-thread
        notifier.multipleDownloadThreads = true

        if (isRunning) {
            // Send the list of downloads to the downloader.
            downloadsSubject.onNext(chaptersToQueue)
        } else {
            // Show initial notification.
            if (notifier.paused) notifier.onDownloadHalted()
            else notifier.onProgressChange(queue)
        }
    }

    /**
     * Stops a download
     */
    fun stop(download: Download): Boolean {
        if (download.downloadStatus !in listOf(DownloadStatus.QUEUE, DownloadStatus.DOWNLOADING))
            return false
        if (download !in queue) return false
        val currentState = isRunning
        if (isRunning) pause()
        download.downloadStatus = DownloadStatus.STOPPED
        return if (currentState) resume() else true
    }

    /**
     * Resumes a download
     */
    fun resume(download: Download): Boolean {
        if (DownloadStatus.STOPPED != download.downloadStatus) return false
        if (download !in queue) return false
        val currentState = isRunning
        if (isRunning) pause()
        download.downloadStatus = DownloadStatus.QUEUE
        return if (currentState) resume() else true
    }

    /**
     * Removes a download from queue
     */
    fun remove(download: Download): Boolean {
        if (download !in queue) return false
        val currentState = isRunning
        if (isRunning) pause()
        queue.remove(download)
        return if (currentState) resume() else true
    }

    /**
     * Destroys the downloader subscriptions.
     */
    private fun destroySubscriptions() {
        if (!isRunning) return
        isRunning = false
        runningSubject.onNext(false)

        subscriptions.clear()
    }

    /**
     * Returns true if the given download can be queued and downloaded.
     *
     * @param download the download to be checked.
     */
    private fun isDownloadAllowed(download: Download): Boolean {
        // If the chapter is already queued, don't add it again
        if (queue.any { it.chapter.id == download.chapter.id })
            return false
        val chapterDir = DownloadProvider.findExistingChapterDir(
                sourceName = download.manga.mangaSourceName,
                mangaTitle = download.manga.mangaTitle,
                chapterTitle = download.chapter.chapterTitle
        )
        if (null != chapterDir) return false
        return true
    }

    /**
     * Returns the observable which downloads a chapter.
     *
     * @param download the chapter to be downloaded.
     */
    @Throws(Exception::class)
    private fun downloadChapter(download: Download): Observable<Download> {
        val chapterDirname = Storage.buildValidFilename(download.chapter.chapterTitle)
        val tmpDir = DownloadProvider.createTempChapterDir(
                sourceName = download.manga.mangaSourceName,
                mangaTitle = download.manga.mangaTitle,
                chapterTitle = download.chapter.chapterTitle
        ) ?: throw Exception("Can not create temporary chapter directory")
        val pageListObservable = if (null == download.pages) {
            // Pull page list from network and add them to download object
            Observable.fromCallable {
                SourceManager
                        .getChapterInfoProcessorForSourceName(download.manga.mangaSourceName)
                        ?.fetchPageList(download.chapter)
                        ?: throw Exception("Error retrieving processor or fetching page list with source:" +
                                " ${download.manga.mangaSourceName}")
            }.doOnNext { pages ->
                if (pages.isEmpty()) {
                    throw Exception("Page list is empty")
                }
                download.pages = pages
            }
        } else {
            // Or if the page list already exists, start from the file
            Observable.just(download.pages!!)
        }
        return pageListObservable
                .doOnNext {
                    // Delete all temporary (unfinished) files
                    tmpDir.listFiles()
                            ?.filter { it.name.endsWith(".tmp") }
                            ?.forEach { it.delete() }

                    download.downloadedImages = 0
                    download.downloadStatus = DownloadStatus.DOWNLOADING
                }
                .flatMap { Observable.from(it) }
                // Start downloading images, consider we can have downloaded images already
                .concatMap { page ->
                    // Adding custom headers to requests
                    val headers = SourceManager
                            .getChapterInfoProcessorForSourceName(download.manga.mangaSourceName)?.headers()
                    // Allow pausing individual download by changing the download's status
                    if (DownloadStatus.DOWNLOADING == download.downloadStatus)
                        getOrDownloadImage(
                                page,
                                download,
                                tmpDir,
                                SourceManager.getChapterInfoProcessorForSourceName(download.manga.mangaSourceName)?.headers()
                                        ?: instance()
                        )
                    else
                        Observable.just(page)
                }
                // Do when page is downloaded.
                .doOnNext { notifier.onProgressChange(download, queue) }
                .toList()
                .map { download }
                // Do after download completes
                .doOnNext { ensureSuccessfulDownload(download, tmpDir, chapterDirname) }
                // If the page list threw, it will resume here
                .onErrorReturn { error ->
                    download.downloadStatus = DownloadStatus.STOPPED
                    notifier.onError(error.message, download.chapter.chapterTitle)
                    download
                }
                .subscribeOn(Schedulers.io())
    }

    /**
     * Returns the observable which gets the image from the filesystem if it exists or downloads it
     * otherwise.
     *
     * @param page the page to download.
     * @param download the download of the page.
     * @param tmpDir the temporary directory of the download.
     */
    private fun getOrDownloadImage(page: Page, download: Download, tmpDir: File, headers: Headers): Observable<Page> {
        // If the image URL is empty, do nothing
        if (page.imageUrls.all { it.isBlank() })
            return Observable.just(page)

        val filename = String.format("%03d", page.pageIndex + 1)
        val tmpFile = tmpDir.findFile("$filename.tmp")

        // Delete temp file if it exists.
        tmpFile?.delete()

        // Try to find the image file.
        val imageFile = tmpDir.findFileIgnoreExtension(fileNameWithoutExtension = filename)

        // If the image is already downloaded, do nothing. Otherwise download from network
        val pageObservable = if (null != imageFile)
            Observable.just(imageFile)
        else
            downloadImage(page, tmpDir, filename, headers)

        return pageObservable
                // When the image is ready, set image path, progress (just in case) and status
                .doOnNext { file ->
                    page.imageFileUri = Uri.fromFile(file).toString()
                    download.downloadedImages++
                    page.pageStatus = DownloadStatus.DOWNLOADED
                }
                .map { page }
                // Mark this page as error and allow to download the remaining
                .onErrorReturn {
                    page.pageStatus = DownloadStatus.STOPPED
                    page
                }
    }

    /**
     * Returns the observable which downloads the image from network.
     *
     * @param page the page to download.
     * @param tmpDir the temporary directory of the download.
     * @param filename the filename of the image.
     */
    private fun downloadImage(page: Page, tmpDir: File, filename: String, headers: Headers): Observable<File> {
        page.pageStatus = DownloadStatus.DOWNLOADING
        return Observable
                .from(
                        page.imageUrls.map {
                            instance<OkHttpClient>("chapter")
                                    .newCall(GET(it, headers = headers))
                                    .asObservableSuccess()
                        }
                )
                .onBackpressureBuffer()
                .concatMap {
                    it
                            .map { response: Response ->
                                val file = tmpDir.createFile("$filename.tmp")
                                try {
                                    response.body?.source()?.inputStream()?.buffered()?.use { inStream ->
                                        file.outputStream().buffered().use { outStream ->
                                            inStream.copyTo(outStream)
                                        }
                                    }
                                    var extension = getImageExtension(response, file)
                                    if (ImageConverter.convertRequiredImageTypes
                                                    .map { imgType -> imgType.substringAfter("image/") }
                                                    .contains(extension)) {
                                        extension = "png"
                                        ImageConverter.convertToSupportedImage(file)
                                    }
                                    file.renameTo("$filename.$extension")
                                } catch (e: Exception) {
                                    response.close()
                                    file.delete()
                                    throw e
                                }
                                file
                            }
                            // Retry 3 times, waiting 2, 4 and 8 seconds between attempts.
                            .retryWhen(RetryWithDelay(3, { attemptNum -> (2 shl attemptNum - 1) * 1000 }, Schedulers.trampoline()))
                            .onErrorReturn { Storage.chapterCacheDir }
                }
                .filter { it != Storage.chapterCacheDir }
                .first()
    }

    /**
     * Returns the extension of the downloaded image from the network response, or if it's null,
     * analyze the file. If everything fails, assume it's a jpg.
     *
     * @param response the network response of the image.
     * @param file the file where the image is already downloaded.
     */
    private fun getImageExtension(response: Response, file: File): String {
        // Read content type if available.
        val mime = response.body?.contentType()?.let { ct -> "${ct.type}/${ct.subtype}" }
        // Else guess from the uri.
                ?: context.contentResolver.getType(file.getUriCompat(context))
                // Else read magic numbers.
                ?: ImageConverter.findImageMime { file.inputStream() }

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
    }

    /**
     * Checks if the download was successful.
     *
     * @param download the download to check.
     * @param tmpDir the directory where the download is currently stored.
     * @param dirname the real (non temporary) directory name of the download.
     */
    private fun ensureSuccessfulDownload(download: Download, tmpDir: File, dirname: String) {
        // Ensure that the chapter folder has all the images.
        val downloadedImages = tmpDir.listFiles().orEmpty().filterNot { it.name.endsWith(".tmp") }

        download.downloadStatus = if (downloadedImages.size == download.pages!!.size) {
            DownloadStatus.DOWNLOADED
        } else {
            DownloadStatus.STOPPED
        }
        // Only rename the directory if it's downloaded.
        if (download.downloadStatus == DownloadStatus.DOWNLOADED) {
            tmpDir.renameTo(dirname)
        }
    }

    /**
     * Completes a download. This method is called in the main thread.
     */
    private fun completeDownload(download: Download) {
        // Delete successful downloads from queue
        if (download.downloadStatus == DownloadStatus.DOWNLOADED) {
            // remove downloaded chapter from queue
            queue.remove(download)
            notifier.onProgressChange(queue)
            // Update downloaded status to database
            download.manga.mangaDownloaded = true
            instance<DatabaseHelper>()
                    .updateDownloadedField(download.manga)
                    .asRxObservable()
                    .first()
                    .subscribeOn(Schedulers.io())
                    .subscribe()
        }
        if (areAllDownloadsFinished()) {
            if (notifier.isSingleChapter && !notifier.errorThrown) {
                notifier.onDownloadCompleted(download, queue)
            }
            DownloadService.stop(context)
        }
    }

    /**
     * Returns true if all the queued downloads are in DOWNLOADED or STOPPED state.
     */
    private fun areAllDownloadsFinished(): Boolean {
        return queue.none { it.downloadStatus <= DownloadStatus.DOWNLOADING }
    }
}
