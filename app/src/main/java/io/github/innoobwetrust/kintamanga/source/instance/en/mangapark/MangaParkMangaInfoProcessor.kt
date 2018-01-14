package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object MangaParkMangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = MangaParkSource

    override var titleSelector: String = "section.manga>div.content>div.hd>h1>a"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = "div.cover>img"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = "p.summary"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = "table.attr>tbody>tr:contains(Alternative)>td"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = "table.attr>tbody>tr:contains(Type)>td"
    override var typesAttribute: String = "text"
    override var genresSelector: String = "table.attr>tbody>tr:contains(Genre(s))>td>a"
    override var genresAttribute: String = "text"
    override var statusSelector: String = "table.attr>tbody>tr:contains(Status)>td"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = "table.attr>tbody>tr:contains(Author(s))>td>a"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = "table.attr>tbody>tr:contains(Artist(s))>td>a"
    override var artistAttribute: String = "text"
    override var teamsSelector: String = ""
    override var teamAttribute: String = ""
    override var warningSelector: String = "section.manga>div.content>div.warning"
    override var warningAttribute: String = "text"
    override var chaptersUriSelector: String = "section.manga>div.content>div#list>div:last-child>div>ul.chapter>li>em>a:last-child"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "section.manga>div.content>div#list>div:last-child>div>ul.chapter>li>span>a"
    override var chaptersTitleAttribute: String = "text"
    override var chaptersDescriptionSelector: String = ""
    override var chaptersDescriptionAttribute: String = ""
    override var chaptersUpdateTimeSelector: String = "section.manga>div.content>div#list>div:last-child>div>ul.chapter>li>i"
    override var chaptersUpdateTimeAttribute: String = "text"
    override var chaptersIndexedDescending: Boolean = true

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()
}