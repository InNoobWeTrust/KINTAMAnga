package io.github.innoobwetrust.kintamanga.model

import java.io.Serializable

interface Manga : Serializable {
    var mangaSourceName: String
    var mangaUri: String
    var mangaTitle: String
    var mangaAlternativeTitle: String
    var mangaDescription: String
    var mangaThumbnailUri: String
    var mangaArtistsString: String
    var mangaAuthorsString: String
    var mangaTranslationTeamsString: String
    var mangaStatus: String
    var mangaTypesString: String
    var mangaGenresString: String
    var mangaWarning: String
    var mangaFavorited: Boolean
    var mangaDownloaded: Boolean
    var mangaViewer: Int
    var mangaLastUpdate: String     // Timestamp in UTC, need to convert to local time to display

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}