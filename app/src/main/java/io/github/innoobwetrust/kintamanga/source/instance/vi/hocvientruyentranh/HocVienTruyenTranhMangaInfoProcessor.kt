package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object HocVienTruyenTranhMangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = HocVienTruyenTranhSource

    override var titleSelector: String = ".__name"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = ".__image>img"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = ".__description>p"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = ".__info>p:contains(Tên khác)"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = ""
    override var typesAttribute: String = ""
    override var genresSelector: String = ".__info>p:contains(Thể loại)>a"
    override var genresAttribute: String = "text"
    override var statusSelector: String = ".__info>p:contains(Tình trạng)"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = ".__info>p:contains(Tác giả)>a"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = ""
    override var artistAttribute: String = ""
    override var teamsSelector: String = ".__info>p:contains(Nhóm dịch)>a"
    override var teamAttribute: String = "text"
    override var warningSelector: String = "div.alert.alert-warning"
    override var warningAttribute: String = "text"
    override var chaptersUriSelector: String = "div.table-scroll>table.table.table-hover>tbody>tr>td>a"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "div.table-scroll>table.table.table-hover>tbody>tr>td>a"
    override var chaptersTitleAttribute: String = "text"
    override var chaptersDescriptionSelector: String = "div.table-scroll>table.table.table-hover>tbody>tr>td:containsOwn(lượt xem)"
    override var chaptersDescriptionAttribute: String = "text"
    override var chaptersUpdateTimeSelector: String = "div.table-scroll>table.table.table-hover>tbody>tr>td:containsOwn(ago)"
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
        mangaAuthorsString = authorsFromDocument(document).joinToString()
        mangaTranslationTeamsString = teamsFromDocument(document).joinToString()
        mangaStatus = statusFromDocument(document).removePrefix("Tình trạng:").trim()
        mangaTypesString = typesFromDocument(document).joinToString()
        mangaGenresString = genresFromDocument(document).joinToString()
        mangaWarning = warningFromDocument(document)
        chapters = chaptersFromDocument(document)
    }
}
