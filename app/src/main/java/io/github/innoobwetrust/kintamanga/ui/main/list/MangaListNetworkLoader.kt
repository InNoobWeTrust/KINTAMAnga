package io.github.innoobwetrust.kintamanga.ui.main.list

import io.github.innoobwetrust.kintamanga.source.model.CatalogPage
import io.github.innoobwetrust.kintamanga.source.model.CatalogPages
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.parallelMap
import rx.Observable
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

interface MangaListNetworkLoader {
    val mangaSegment: SourceSegment
    val mangaInfoProcessor: MangaInfoProcessor
    var catalogPages: CatalogPages

    var userInput: Map<String, String>
    var singleChoice: Map<String, String>
    var multipleChoices: Set<Pair<String, String>>

    var refreshDisposable: Subscription?
    var loadNextPageDisposable: Subscription?
    var loadMissingInfoDisposable: Subscription?

    @Throws(Exception::class)
    private fun loadCatalogPage(): CatalogPage {
        return mangaSegment.fetchCatalogPage(
                pageNumber = 1,
                userInput = userInput,
                singleChoice = singleChoice,
                multipleChoices = multipleChoices
        )
    }

    @Throws(Exception::class)
    private fun networkLoadMissingMangaInfo(elementInfo: ElementInfo): MangaBinding =
            mangaInfoProcessor.fetchManga(elementInfo.itemUri)

    fun requestFilter()

    fun backgroundRefresh(
            onRefreshedCatalogPage: (CatalogPage) -> Unit,
            onRefreshError: (Throwable) -> Unit
    ) {
        // Prevent conflict
        stopLoadingNextPage()
        stopLoadingMissingInfo()
        // Real job
        refreshDisposable = Single.fromCallable { loadCatalogPage() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { catalogPage -> onRefreshedCatalogPage(catalogPage) },
                        { error -> onRefreshError(error) }
                )
    }

    fun backgroundLoadNextCatalogPage(
            onNextCatalogPage: (CatalogPage?) -> Unit,
            onNextCatalogPageError: (Throwable) -> Unit
    ) {
        if (catalogPages.isReady) {
            // Prevent conflict
            stopRefresh()
            // Real job
            loadNextPageDisposable = Single.fromCallable { catalogPages.loadNext() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { catalogPage -> onNextCatalogPage(catalogPage) },
                            { error -> onNextCatalogPageError(error) }
                    )
        }
    }

    @Throws(Exception::class)
    fun backgroundLoadMissingInfo(
            onNextMissingMangaInfoLoaded: (MangaBinding) -> Unit,
            onMissingMangaInfoError: (Throwable) -> Unit
    ) {
        if (catalogPages.isReady) {
            // Prevent conflict
            stopLoadingMissingInfo()
            // Real job
            loadMissingInfoDisposable = Observable.from(catalogPages.elementInfos)
                    .filter { it.itemThumbnailUri.isBlank() }
                    .parallelMap { elementInfo ->
                        Observable.fromCallable { networkLoadMissingMangaInfo(elementInfo) }
                                .onErrorReturn { MangaBinding() }
                                .filter { !it.mangaThumbnailUri.isBlank() }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { mangaBinding -> onNextMissingMangaInfoLoaded(mangaBinding) },
                            { error -> onMissingMangaInfoError(error) }
                    )
        }
    }

    fun stopRefresh() {
        refreshDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }

    fun stopLoadingNextPage() {
        loadNextPageDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }

    fun stopLoadingMissingInfo() {
        loadMissingInfoDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }

    fun disposeAllLoaderDisposables() {
        stopRefresh()
        stopLoadingNextPage()
        stopLoadingMissingInfo()
    }
}