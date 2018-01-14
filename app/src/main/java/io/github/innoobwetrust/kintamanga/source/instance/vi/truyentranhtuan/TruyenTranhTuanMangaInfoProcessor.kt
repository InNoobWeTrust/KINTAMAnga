package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object TruyenTranhTuanMangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = TruyenTranhTuanSource

    override var titleSelector: String = "h1[itemprop=name]"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = "div.manga-cover>img"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = "#manga-summary>p"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = "p.misc-infor:containsOwn(Tên khác:)"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = ""
    override var typesAttribute: String = ""
    override var genresSelector: String = "p.misc-infor:containsOwn(Thể loại:)>a"
    override var genresAttribute: String = "text"
    override var statusSelector: String = "p.misc-infor:containsOwn(Chương mới nhất)>a"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = "p.misc-infor:containsOwn(Tác giả:)"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = ""
    override var artistAttribute: String = ""
    override var teamsSelector: String = ""
    override var teamAttribute: String = ""
    override var warningSelector: String = ""
    override var warningAttribute: String = ""
    override var chaptersUriSelector: String = "#manga-chapter>span.chapter-name>a"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "#manga-chapter>span.chapter-name"
    override var chaptersTitleAttribute: String = "text"
    override var chaptersDescriptionSelector: String = "#manga-chapter>span.group-name"
    override var chaptersDescriptionAttribute: String = "text"
    override var chaptersUpdateTimeSelector: String = "#manga-chapter>span.date-name"
    override var chaptersUpdateTimeAttribute: String = "text"
    override var chaptersIndexedDescending: Boolean = true

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun mangaFromDocument(document: Document): MangaBinding? = MangaBinding().apply {
        mangaSourceName = source.sourceName
        mangaUri = document.baseUri()
        mangaTitle = titleFromDocument(document)
        mangaAlternativeTitle = alternativeTitleFromDocument(document).removePrefix("Tên khác:").trim()
        mangaDescription = descriptionFromDocument(document)
        mangaThumbnailUri = thumbnailUriFromDocument(document)
        mangaArtistsString = artistsFromDocument(document).joinToString()
        mangaAuthorsString = authorsFromDocument(document).joinToString().removePrefix("Tác giả:").trim()
        mangaTranslationTeamsString = teamsFromDocument(document).joinToString()
        mangaStatus = statusFromDocument(document)
        mangaTypesString = typesFromDocument(document).joinToString()
        mangaGenresString = genresFromDocument(document).joinToString()
        mangaWarning = warningFromDocument(document)
        chapters = chaptersFromDocument(document)
    }
}