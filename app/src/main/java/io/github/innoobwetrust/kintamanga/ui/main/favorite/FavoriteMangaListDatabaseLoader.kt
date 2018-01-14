package io.github.innoobwetrust.kintamanga.ui.main.favorite

import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

interface FavoriteMangaListDatabaseLoader {
    val databaseHelper: DatabaseHelper
    var observeDatabaseDisposable: Subscription?

    var sourceNameFilter: String?

    @Throws(Exception::class)
    fun observeDataBase(
            groupType: Int,
            onNextDatabaseChange: (List<MangaDb>) -> Unit,
            onDatabaseError: (Throwable) -> Unit
    ) {
        if (groupType !in 0..2) throw Exception("groupType invalid")
        observeDatabaseDisposable = databaseHelper
                .run {
                    when (groupType) {
                        0 -> getFavoriteMangas()
                        1 -> getDownloadedMangas()
                        2 -> getMangas()
                        else -> getMangas()
                    }
                }
                .asRxObservable()
                .map { listMangaDb ->
                    if (null != sourceNameFilter)
                        listMangaDb.filter { sourceNameFilter == it.mangaSourceName }
                    else
                        listMangaDb
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { listMangaDb ->
                            onNextDatabaseChange(listMangaDb)
                        },
                        { error -> onDatabaseError(error) }
                )
    }

    fun disposeAllLoaderDisposables() {
        observeDatabaseDisposable?.let { if (!it.isUnsubscribed) it.unsubscribe() }
    }
}