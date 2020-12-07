package io.github.innoobwetrust.kintamanga.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import io.github.innoobwetrust.kintamanga.BR
import rx.subjects.PublishSubject
import java.io.Serializable

class Page(
        var chapterIndex: Int = -1,
        val pageIndex: Int,
        var imageUrls: List<String> = listOf(""),
        var imageFileUri: String = "",
) : BaseObservable(), Serializable {

    init {
        if (imageUrls.isEmpty()) throw Exception("imageUrls for this page must not be empty!")
    }

    @get:Bindable
    @Transient
    @Volatile
    var pageStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED
        set(value) {
            field = value
            statusSubject?.onNext(value)
            notifyPropertyChanged(BR.pageStatus)
        }

    @Transient
    private var statusSubject: PublishSubject<DownloadStatus>? = null

    fun setStatusSubject(subject: PublishSubject<DownloadStatus>?) {
        this.statusSubject = subject
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Page) return false
        return other.chapterIndex == chapterIndex && other.pageIndex == pageIndex
    }

    override fun hashCode(): Int {
        return if (-1 >= chapterIndex) chapterIndex else chapterIndex * 1000 + pageIndex
    }
}
