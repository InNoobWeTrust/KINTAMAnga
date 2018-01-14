package io.github.innoobwetrust.kintamanga.ui.model

import androidx.databinding.Bindable
import io.github.innoobwetrust.kintamanga.BR
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.model.Page

class ChapterBinding : ChapterDb() {
    @get:Bindable
    var chapterDownloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterDownloadStatus)
        }

    var chapterPages = emptyList<Page>()

    @get:Bindable
    override var chapterUri: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterUri)
        }

    @get:Bindable
    override var chapterTitle: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterTitle)
        }

    @get:Bindable
    override var chapterDescription: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterDescription)
        }

    @get:Bindable
    override var chapterUpdateTime: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterUpdateTime)
        }

    override var chapterIndex: Int = -1

    @get:Bindable
    override var chapterViewed: Boolean = false
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.chapterViewed)
        }

    @get:Bindable
    override var chapterLastPageRead: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.chapterLastPageRead)
        }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ChapterBinding) return false
        return other.chapterUri == this.chapterUri
    }

    override fun hashCode(): Int {
        return chapterUri.hashCode()
    }
}
