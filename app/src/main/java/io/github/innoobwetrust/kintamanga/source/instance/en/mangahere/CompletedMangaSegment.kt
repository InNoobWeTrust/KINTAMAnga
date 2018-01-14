package io.github.innoobwetrust.kintamanga.source.instance.en.mangahere

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object CompletedMangaSegment : DomSegment {
    override var source: Source = MangaHereSource
    override var pathName: String = "Completed Manga"
    override var pathSegment: String = "completed"

    override var filterKeyLabel: Map<String, String> = mapOf(
            "order" to "Order by"
    )
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = mapOf(
            "order" to mapOf(
                    "Name A-Z" to "?name.az",
                    "Name Z-A" to "?name.za",
                    "Rating A-Z" to "?rating.az",
                    "Rating Z-A" to "?rating.za",
                    "Views A-Z" to "?views.az",
                    "Views Z-A" to "?views.za",
                    "Latest Updated A-Z" to "?last_chapter_time.az",
                    "Latest Updated Z-A" to "?last_chapter_time.za"
            )
    )
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "order" to "?last_chapter_time.az"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "body>section>article>div>div.directory_list>ul>li>div>div>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "body>section>article>div>div.directory_list>ul>li>div>div>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "body>section>article>div>div.directory_list>ul>li>a>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "body>section>article>div>div.directory_list>ul>li>div"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "body>section>article>div>div.directory_list>div.directory_footer>div>a.prew"
    override var nextPageSelector: String = "body>section>article>div>div.directory_list>div.directory_footer>div>a.next"

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
        val pagePathString = "${if (page > 1) "$page.htm" else "/"}${
        singleChoice["order"] ?: filterRequiredDefaultSingleChoice["order"]
        }"
        return "${urlBuilder.build()}$pagePathString".toHttpUrlOrNull()
                ?: throw Exception("${source.sourceName} - $pathName: Failed to append option to url")
    }
}
