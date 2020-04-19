package io.github.innoobwetrust.kintamanga.source.instance.en.mangafox

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object MangaDirectorySegment : DomSegment {
    override var source: Source = MangaFoxSource
    override var pathName: String = "Manga Directory"
    override var pathSegment: String = "directory"

    override var filterKeyLabel: Map<String, String> = mapOf(
            "order" to "Order by"
    )
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = mapOf(
            "order" to mapOf(
                    "Alphabetical" to "?az",
                    "Popularity" to "",
                    "Rating" to "?rating",
                    "Latest Chapters" to "?latest"
            )
    )
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "order" to "?latest"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "ul.list>li>a.manga_img"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "ul.list>li>div.manga_text>a.title"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "ul.list>li>a.manga_img>div>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "ul.list>li>div.manga_text>p.nowrap.latest"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "div#nav>ul>li:contains(Prev)>a"
    override var nextPageSelector: String = "div#nav>ul>li:contains(Next)>a"

    override fun buildURL(
            page: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>
    ): HttpUrl {
        if (page < 1) throw IllegalArgumentException(
                "invalid page number"
        )
        val urlBuilder: HttpUrl.Builder = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.addEncodedPathSegments(pathSegment)
                ?: throw Exception("${source.sourceName} - $pathName: Failed to generate url")
        val pagePathString = "${if (page > 1) "$page.htm" else ""}${
        singleChoice["order"] ?: filterRequiredDefaultSingleChoice["order"]
        }"
        return "${urlBuilder.build()}$pagePathString".toHttpUrlOrNull()
                ?: throw Exception("${source.sourceName} - $pathSegment: Failed to append option to url")
    }
}
