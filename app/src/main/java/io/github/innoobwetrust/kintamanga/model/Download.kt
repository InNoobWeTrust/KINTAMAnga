package io.github.innoobwetrust.kintamanga.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import io.github.innoobwetrust.kintamanga.BR
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import rx.subjects.PublishSubject

class Download(val manga: MangaDb, val chapter: ChapterDb): BaseObservable() {
    @get:Bindable
    var pages: List<Page>? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.pages)
        }

    @get:Bindable
    @Volatile @Transient var downloadedImages: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.downloadedImages)
        }

    @get:Bindable
    @Volatile @Transient var downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED
        set(status) {
            field = status
            statusSubject?.onNext(this)
            notifyPropertyChanged(BR.downloadStatus)
        }

    @Transient private var statusSubject: PublishSubject<Download>? = null

    fun setStatusSubject(subject: PublishSubject<Download>?) {
        statusSubject = subject
    }
}
