package io.github.innoobwetrust.kintamanga.ui.model

import androidx.databinding.Bindable
import io.github.innoobwetrust.kintamanga.BR
import io.github.innoobwetrust.kintamanga.database.model.MangaDb

class MangaBinding : MangaDb() {
    var chapters: List<ChapterBinding> = emptyList()
        set(value) {
            field = value.onEach { it.mangaId = this.id }
        }

    @get:Bindable
    var mangaIdIsNotNull: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.mangaIdIsNotNull)
        }

    override var id: Long? = null
        set(value) {
            field = value
            mangaIdIsNotNull = null != field
            chapters.onEach { it.mangaId = value }
        }
    override var mangaSourceName: String = ""

    @get:Bindable
    override var mangaUri: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaUri)
        }

    @get:Bindable
    override var mangaTitle: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaTitle)
        }

    @get:Bindable
    override var mangaAlternativeTitle: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaAlternativeTitle)
        }

    @get:Bindable
    override var mangaDescription: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaDescription)
        }

    @get:Bindable
    override var mangaThumbnailUri: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaThumbnailUri)
        }

    @get:Bindable
    override var mangaArtistsString: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaArtistsString)
        }

    @get:Bindable
    override var mangaAuthorsString: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaAuthorsString)
        }
    @get:Bindable
    override var mangaTranslationTeamsString: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaTranslationTeamsString)
        }

    @get:Bindable
    override var mangaStatus = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaStatus)
        }

    @get:Bindable
    override var mangaTypesString: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaTypesString)
        }

    @get:Bindable
    override var mangaGenresString: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaGenresString)
        }

    @get:Bindable
    override var mangaWarning: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaWarning)
        }

    @get:Bindable
    override var mangaFavorited: Boolean = false
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaFavorited)
        }

    @get:Bindable
    override var mangaDownloaded: Boolean = false
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaDownloaded)
        }

    @get:Bindable
    override var mangaViewer: Int = 0
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaViewer)
        }

    @get:Bindable
    override var mangaLastUpdate: String = ""
        set(value) {
            if (value == field) return
            field = value
            notifyPropertyChanged(BR.mangaLastUpdate)
        }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MangaBinding) return false
        return other.mangaUri == this.mangaUri
    }

    override fun hashCode(): Int {
        return mangaUri.hashCode()
    }
}
