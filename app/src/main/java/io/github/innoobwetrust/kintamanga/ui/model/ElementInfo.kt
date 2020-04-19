package io.github.innoobwetrust.kintamanga.ui.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import io.github.innoobwetrust.kintamanga.BR
import java.io.Serializable


class ElementInfo : BaseObservable(), Serializable {

    @get:Bindable
    var sourceName: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.sourceName)
        }

    @get:Bindable
    var itemUri: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.itemUri)
        }

    @get:Bindable
    var itemTitle: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.itemTitle)
        }

    @get:Bindable
    var itemDescription: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.itemDescription)
        }

    @get:Bindable
    var itemThumbnailUri: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.itemThumbnailUri)
        }

    fun asMangaBinding(): MangaBinding = MangaBinding().apply {
        mangaSourceName = sourceName
        mangaUri = itemUri
        mangaTitle = itemTitle
        mangaDescription = itemDescription
        mangaThumbnailUri = itemThumbnailUri
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ElementInfo) return false
        return other.itemUri == this.itemUri
    }

    override fun hashCode(): Int {
        return itemUri.hashCode()
    }
}
