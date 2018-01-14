package io.github.innoobwetrust.kintamanga.source.dom.parser

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import io.github.innoobwetrust.kintamanga.util.extension.parseString
import io.github.innoobwetrust.kintamanga.util.extension.parseUri
import org.jsoup.nodes.Document

interface DomMangaParser {
    var source: Source

    var titleSelector: String
    var titleAttribute: String
    var thumbnailUriSelector: String
    var thumbnailUriAttribute: String
    var descriptionSelector: String
    var descriptionAttribute: String
    var alternativeTitleSelector: String
    var alternativeTitleAttribute: String
    var typesSelector: String
    var typesAttribute: String
    var genresSelector: String
    var genresAttribute: String
    var statusSelector: String
    var statusAttribute: String
    var authorsSelector: String
    var authorsAttribute: String
    var artistsSelector: String
    var artistAttribute: String
    var teamsSelector: String
    var teamAttribute: String
    var warningSelector: String
    var warningAttribute: String
    var chaptersTitleSelector: String
    var chaptersTitleAttribute: String
    var chaptersUriSelector: String
    var chaptersUriAttribute: String
    var chaptersDescriptionSelector: String
    var chaptersDescriptionAttribute: String
    var chaptersUpdateTimeSelector: String
    var chaptersUpdateTimeAttribute: String
    var chaptersIndexedDescending: Boolean

    fun titleFromDocument(document: Document): String = document.parseString(
            selector = titleSelector,
            attribute = titleAttribute
    )

    fun thumbnailUriFromDocument(document: Document): String = document.parseUri(
            selector = thumbnailUriSelector,
            attribute = thumbnailUriAttribute
    )

    fun descriptionFromDocument(document: Document): String = document.parseString(
            selector = descriptionSelector,
            attribute = descriptionAttribute
    )

    fun alternativeTitleFromDocument(document: Document): String = document.parseString(
            selector = alternativeTitleSelector,
            attribute = alternativeTitleAttribute
    )

    fun typesFromDocument(document: Document): List<String> =
            if (typesSelector.isBlank()) emptyList() else document.parseListString(
                    selector = typesSelector,
                    attribute = typesAttribute
            )

    fun genresFromDocument(document: Document): List<String> =
            if (genresSelector.isBlank()) emptyList() else document.parseListString(
                    selector = genresSelector,
                    attribute = genresAttribute
            )

    fun statusFromDocument(document: Document): String = document.parseString(
            selector = statusSelector,
            attribute = statusAttribute
    )

    fun authorsFromDocument(document: Document): List<String> =
            if (authorsSelector.isBlank()) emptyList() else document.parseListString(
                    selector = authorsSelector,
                    attribute = authorsAttribute
            )

    fun artistsFromDocument(document: Document): List<String> =
            if (artistsSelector.isBlank()) emptyList() else document.parseListString(
                    selector = artistsSelector,
                    attribute = artistAttribute
            )

    fun teamsFromDocument(document: Document): List<String> =
            if (teamsSelector.isBlank()) emptyList() else document.parseListString(
                    selector = teamsSelector,
                    attribute = teamAttribute
            )

    fun warningFromDocument(document: Document): String = document.parseString(
            selector = warningSelector,
            attribute = warningAttribute
    )

    fun chaptersFromDocument(document: Document): List<ChapterBinding> {
        val uris = document.parseListUri(
                selector = chaptersUriSelector,
                attribute = chaptersUriAttribute
        )
        val titles = document.parseListString(
                selector = chaptersTitleSelector,
                attribute = chaptersTitleAttribute
        )
        val descriptions = document.parseListString(
                selector = chaptersDescriptionSelector,
                attribute = chaptersDescriptionAttribute
        )
        val updateTimes = document.parseListString(
                selector = chaptersUpdateTimeSelector,
                attribute = chaptersUpdateTimeAttribute
        )
        return uris.mapIndexed { index, s ->
            ChapterBinding().apply {
                chapterUri = s
                chapterTitle = titles.getOrElse(index) { "" }
                chapterDescription = descriptions.getOrElse(index) { "" }
                chapterUpdateTime = updateTimes.getOrElse(index) { "" }
                chapterIndex = if (chaptersIndexedDescending) uris.size - index - 1 else index
            }
        }.sortedBy { it.chapterIndex }
    }

    fun mangaFromDocument(document: Document): MangaBinding? = MangaBinding().apply {
        mangaSourceName = source.sourceName
        mangaUri = document.baseUri()
        mangaTitle = titleFromDocument(document)
        mangaAlternativeTitle = alternativeTitleFromDocument(document)
        mangaDescription = descriptionFromDocument(document)
        mangaThumbnailUri = thumbnailUriFromDocument(document)
        mangaArtistsString = artistsFromDocument(document).joinToString()
        mangaAuthorsString = authorsFromDocument(document).joinToString()
        mangaTranslationTeamsString = teamsFromDocument(document).joinToString()
        mangaStatus = statusFromDocument(document)
        mangaTypesString = typesFromDocument(document).joinToString()
        mangaGenresString = genresFromDocument(document).joinToString()
        mangaWarning = warningFromDocument(document)
        chapters = chaptersFromDocument(document)
    }
}