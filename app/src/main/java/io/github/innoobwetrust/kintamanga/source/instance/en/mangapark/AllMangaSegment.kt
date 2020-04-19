package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object AllMangaSegment : DomSegment {
    override var source: Source = MangaParkSource
    override var pathName: String = "All Manga"
    override var pathSegment: String = "genre"

    override var filterKeyLabel: Map<String, String> = mapOf(
            "order" to "Order by"
    )
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = mapOf(
            "order" to mapOf(
                    "A-Z" to "a-z",
                    "Views" to "views",
                    "Rating" to "rating",
                    "Latest" to "latest",
                    "New manga" to "add"
            )
    )
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "order" to "latest"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "section.genre>div.content>div.ls1>div.item>a.cover"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "section.genre>div.content>div.ls1>div.item>ul>h3>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "section.genre>div.content>div.ls1>div.item>a.cover>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "section.genre>div.content>div.ls1>div.item>ul>li.b.new>a"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "section.genre>div.content>div#paging-bar>ul.paging.full>li:contains(◀prev)>a"
    override var nextPageSelector: String = "section.genre>div.content>div#paging-bar>ul.paging.full>li:contains(next▶)>a"

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
                ?.addEncodedPathSegments(page.toString())
                ?: throw Exception("Failed to generate url")
        singleChoice.forEach { urlBuilder.addQueryParameter(it.value, "") }
        return urlBuilder.build()
    }
}
