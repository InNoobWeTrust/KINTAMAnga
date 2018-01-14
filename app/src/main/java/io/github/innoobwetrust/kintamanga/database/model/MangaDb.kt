package io.github.innoobwetrust.kintamanga.database.model

import androidx.databinding.BaseObservable
import io.github.innoobwetrust.kintamanga.model.Manga
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding

open class MangaDb : BaseObservable(), Manga {
    open var id: Long? = null
    open var flags: Int = 0
    override var mangaSourceName: String = ""
    override var mangaUri: String = ""
    override var mangaTitle: String = ""
    override var mangaAlternativeTitle: String = ""
    override var mangaDescription: String = ""
    override var mangaThumbnailUri: String = ""
    override var mangaArtistsString: String = ""
    override var mangaAuthorsString: String = ""
    override var mangaTranslationTeamsString: String = ""
    override var mangaStatus: String = ""
    override var mangaTypesString: String = ""
    override var mangaGenresString: String = ""
    override var mangaWarning: String = ""
    override var mangaFavorited: Boolean = false
    override var mangaDownloaded: Boolean = false
    override var mangaViewer: Int = -1
    override var mangaLastUpdate: String = ""

    fun copyTo(mangaDb: MangaDb) {
        mangaDb.let {
            it.id = this@MangaDb.id
            it.flags = this@MangaDb.flags
            it.mangaSourceName = this@MangaDb.mangaSourceName
            it.mangaUri = this@MangaDb.mangaUri
            it.mangaTitle = this@MangaDb.mangaTitle
            it.mangaAlternativeTitle = this@MangaDb.mangaAlternativeTitle
            it.mangaDescription = this@MangaDb.mangaDescription
            it.mangaThumbnailUri = this@MangaDb.mangaThumbnailUri
            it.mangaArtistsString = this@MangaDb.mangaArtistsString
            it.mangaAuthorsString = this@MangaDb.mangaAuthorsString
            it.mangaTranslationTeamsString = this@MangaDb.mangaTranslationTeamsString
            it.mangaStatus = this@MangaDb.mangaStatus
            it.mangaTypesString = this@MangaDb.mangaTypesString
            it.mangaGenresString = this@MangaDb.mangaGenresString
            it.mangaWarning = this@MangaDb.mangaWarning
            it.mangaFavorited = this@MangaDb.mangaFavorited
            it.mangaDownloaded = this@MangaDb.mangaDownloaded
            it.mangaViewer = this@MangaDb.mangaViewer
            it.mangaLastUpdate = this@MangaDb.mangaLastUpdate
        }
    }

    fun copyFrom(mangaDb: MangaDb) {
        mangaDb.let {
            this@MangaDb.id = it.id
            this@MangaDb.flags = it.flags
            this@MangaDb.mangaSourceName = it.mangaSourceName
            this@MangaDb.mangaUri = it.mangaUri
            this@MangaDb.mangaTitle = it.mangaTitle
            this@MangaDb.mangaAlternativeTitle = it.mangaAlternativeTitle
            this@MangaDb.mangaDescription = it.mangaDescription
            this@MangaDb.mangaThumbnailUri = it.mangaThumbnailUri
            this@MangaDb.mangaArtistsString = it.mangaArtistsString
            this@MangaDb.mangaAuthorsString = it.mangaAuthorsString
            this@MangaDb.mangaTranslationTeamsString = it.mangaTranslationTeamsString
            this@MangaDb.mangaStatus = it.mangaStatus
            this@MangaDb.mangaTypesString = it.mangaTypesString
            this@MangaDb.mangaGenresString = it.mangaGenresString
            this@MangaDb.mangaWarning = it.mangaWarning
            this@MangaDb.mangaFavorited = it.mangaFavorited
            this@MangaDb.mangaDownloaded = it.mangaDownloaded
            this@MangaDb.mangaViewer = it.mangaViewer
            this@MangaDb.mangaLastUpdate = it.mangaLastUpdate
        }
    }

    fun asMangaBinding(): MangaBinding = MangaBinding().also { it.copyFrom(this@MangaDb) }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is MangaDb) return false
        return other.mangaUri == this.mangaUri
    }

    override fun hashCode(): Int {
        return mangaUri.hashCode()
    }
}
