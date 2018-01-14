package io.github.innoobwetrust.kintamanga.source.instance.en.mangafox

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object MangaFoxMangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = MangaFoxSource

    override var titleSelector: String = "div#title>h1"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = "div#series_info>div.cover>img"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = "p.summary"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = "div#title>h3"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = ""
    override var typesAttribute: String = ""
    override var genresSelector: String = "div#title>table>tbody>tr:last-child>td:nth-child(4)>a"
    override var genresAttribute: String = "text"
    override var statusSelector: String = "div.data:nth-child(5)>span:nth-child(2)"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = "div#title>table>tbody>tr:last-child>td:nth-child(2)>a"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = "div#title>table>tbody>tr:last-child>td:nth-child(3)>a"
    override var artistAttribute: String = "text"
    override var teamsSelector: String = "div.data:nth-child(8)>span>a"
    override var teamAttribute: String = "text"
    override var warningSelector: String = "div.warning"
    override var warningAttribute: String = "text"
    override var chaptersUriSelector: String = "div#chapters>ul.chlist>li>div>h3>a"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "div#chapters>ul.chlist>li>div>h3>a"
    override var chaptersTitleAttribute: String = "text"
    override var chaptersDescriptionSelector: String = ""
    override var chaptersDescriptionAttribute: String = ""
    override var chaptersUpdateTimeSelector: String = "div#chapters>ul.chlist>li>div>span"
    override var chaptersUpdateTimeAttribute: String = "text"
    override var chaptersIndexedDescending: Boolean = true

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun chaptersFromDocument(document: Document): List<ChapterBinding> {
        val uris = document.parseListUri(
                selector = chaptersUriSelector,
                attribute = chaptersUriAttribute
        ).map {
            it.replaceFirst(
                    oldValue = "http://mangafox.me/manga/",
                    newValue = "http://m.mangafox.me/roll_manga/"
            ).replace("/[0-9]+.html".toRegex(), "")
        }
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
}