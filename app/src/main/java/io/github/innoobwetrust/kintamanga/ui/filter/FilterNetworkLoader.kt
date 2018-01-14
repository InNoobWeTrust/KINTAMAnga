package io.github.innoobwetrust.kintamanga.ui.filter

import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

interface FilterNetworkLoader {
    var mangaSegment: SourceSegment

    var refreshDisposable: Subscription?

    fun backgroundRefreshFilterData(
            onRefreshedFilterData: (Boolean) -> Unit,
            onRefreshError: (Throwable) -> Unit
    ) {
        disposeAllLoaderDisposables()
        refreshDisposable = Single.fromCallable { mangaSegment.fetchFilterData() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {success -> onRefreshedFilterData(success)},
                        {error -> onRefreshError(error)}
                )
    }

    fun disposeAllLoaderDisposables() {
        refreshDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }
}