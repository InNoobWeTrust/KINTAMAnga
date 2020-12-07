package io.github.innoobwetrust.kintamanga.source.instance.en.mangahere

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import io.github.innoobwetrust.kintamanga.util.extension.parseString
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object MangaHereMangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = MangaHereSource

    override var titleSelector: String = "#main>article>div>div.box_w.clearfix>h1.title"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>img"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = "#show"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>ul>li:contains(Alternative Name:)"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = ""
    override var typesAttribute: String = ""
    override var genresSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>ul>li:contains(Genre(s):)"
    override var genresAttribute: String = "text"
    override var statusSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>ul>li:contains(Status:)"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>ul>li:contains(Author(s):)>a"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = "#main>article>div>div.manga_detail>div.manga_detail_top.clearfix>ul>li:contains(Artist(s):)>a"
    override var artistAttribute: String = "text"
    override var teamsSelector: String = ""
    override var teamAttribute: String = ""
    override var warningSelector: String = ""
    override var warningAttribute: String = ""
    override var chaptersUriSelector: String = "#main>article>div>div.manga_detail>div.detail_list>ul>li>span.left>a"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "#main>article>div>div.manga_detail>div.detail_list>ul>li>span.left>a"
    override var chaptersTitleAttribute: String = "text"
    override var chaptersDescriptionSelector: String = "#main>article>div>div.manga_detail>div.detail_list>ul>li>span.left"
    override var chaptersDescriptionAttribute: String = "text"
    override var chaptersUpdateTimeSelector: String = "#main>article>div>div.manga_detail>div.detail_list>ul>li>span.right"
    override var chaptersUpdateTimeAttribute: String = "text"
    override var chaptersIndexedDescending: Boolean = true

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun mangaFromDocument(document: Document): MangaBinding = MangaBinding().apply {
        mangaSourceName = source.sourceName
        mangaUri = document.baseUri()
        mangaTitle = titleFromDocument(document)
        mangaAlternativeTitle = alternativeTitleFromDocument(document).removePrefix("Alternative Name:").trim()
        mangaDescription = descriptionFromDocument(document).removeSuffix("Show less").trim()
        mangaThumbnailUri = thumbnailUriFromDocument(document)
        mangaArtistsString = artistsFromDocument(document).joinToString()
        mangaAuthorsString = authorsFromDocument(document).joinToString()
        mangaTranslationTeamsString = teamsFromDocument(document).joinToString()
        mangaStatus = statusFromDocument(document).removePrefix("Status:").trim()
        mangaTypesString = typesFromDocument(document).joinToString()
        mangaGenresString = if (genresSelector.isBlank()) "" else document.parseString(
                selector = genresSelector,
                attribute = genresAttribute
        ).removePrefix("Genre(s):").trim()
        mangaWarning = warningFromDocument(document)
        chapters = chaptersFromDocument(document)
    }

    override fun chaptersFromDocument(document: Document): List<ChapterBinding> {
        val uris = document.parseListUri(
                selector = chaptersUriSelector,
                attribute = chaptersUriAttribute
        ).map {
            it.replaceFirst(
                    oldValue = "http://www.mangahere.co/manga/",
                    newValue = "http://m.mangahere.co/roll_manga/"
            )
        }
        val titles = document.parseListString(
                selector = chaptersTitleSelector,
                attribute = chaptersTitleAttribute
        )
        val descriptions = document.parseListString(
                selector = chaptersDescriptionSelector,
                attribute = chaptersDescriptionAttribute
        ).mapIndexed { index, s ->
            s.removePrefix(titles.getOrElse(index) { "" }).trim()
        }
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
}
