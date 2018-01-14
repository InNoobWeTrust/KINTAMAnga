package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Document
import timber.log.Timber

object SearchSegment : DomSegment {
    override var source: Source = MangaParkSource
    override var pathName: String = "Search"
    override var pathSegment: String = "search"

    override var filterKeyLabel: Map<String, String> = mapOf(
            // User input
            "q" to "Title",
            "autart" to "Author/Artist",
            // Single choice
            "name-match" to "Title matching strategy",
            "autart-match" to "Author/Artist matching strategy",
            "orderby" to "Order by",
            "genres-mode" to "Genre Inclusion",
            "chapters" to "Chapters",
            "status" to "Status",
            "rating" to "Rating",
            "types" to "Type",
            "years" to "Release",
            // Multiple choices
            "genres" to "Genres",
            "genres-exclude" to "Exclude genres"
    )
    override var filterByUserInput: List<String> = listOf("q", "autart")
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
        get() {
            if (!isFilterDataSingleChoiceFinalized) {
                fetchFilterData()
            }
            return field
        }
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
        get() {
            if (!isFilterDataMultipleChoicesFinalized) {
                fetchFilterData()
            }
            return field
        }
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = mapOf(
            "name-match" to listOf(
                    "#name-match>option",
                    "text",
                    "#name-match>option",
                    "value"
            ),
            "autart-match" to listOf(
                    "#autart-match>option",
                    "text",
                    "#autart-match>option",
                    "value"
            ),
            "orderby" to listOf(
                    "div#paging-bar>ul.paging.order:contains(Order by:)>li[rel]>a",
                    "text",
                    "div#paging-bar>ul.paging.order:contains(Order by:)>li[rel]",
                    "rel"
            ),
            "genres-mode" to listOf(
                    "#filter-option-cnt ul.genres-mode.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.genres-mode.opt-item.triploid.radio>li>span",
                    "rel"
            ),
            "chapters" to listOf(
                    "#filter-option-cnt ul.chapters.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.chapters.opt-item.triploid.radio>li>span",
                    "rel"
            ),
            "status" to listOf(
                    "#filter-option-cnt ul.status.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.status.opt-item.triploid.radio>li>span",
                    "rel"
            ),
            "rating" to listOf(
                    "#filter-option-cnt ul.rating.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.rating.opt-item.triploid.radio>li>span",
                    "rel"
            ),
            "types" to listOf(
                    "#filter-option-cnt ul.types.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.types.opt-item.triploid.radio>li>span",
                    "rel"
            ),
            "years" to listOf(
                    "#filter-option-cnt ul.years.opt-item.triploid.radio>li>span",
                    "text",
                    "#filter-option-cnt ul.years.opt-item.triploid.radio>li>span",
                    "rel"
            )
    )
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = mapOf(
            "genres" to listOf(
                    "#filter-option-cnt ul.genres.opt-item.triploid>li>span",
                    "text",
                    "#filter-option-cnt ul.genres.opt-item.triploid>li>span",
                    "rel"
            ),
            "genres-exclude" to listOf(
                    "#filter-option-cnt ul.genres.opt-item.triploid>li>span",
                    "text",
                    "#filter-option-cnt ul.genres.opt-item.triploid>li>span",
                    "rel"
            )
    )
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "name-match" to "contain",
            "autart-match" to "contain",
            "orderby" to "a-z",
            "genres-mode" to "",
            "chapters" to "",
            "status" to "",
            "rating" to "",
            "types" to "",
            "years" to ""
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = false
    override var isFilterDataMultipleChoicesFinalized: Boolean = false
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "section.search>div.content>div.manga-list>div.item>table>tbody>tr>td>h2>a[target=_blank]"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "section.search>div.content>div.manga-list>div.item>table>tbody>tr>td>h2>a[target=_blank]"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "section.search>div.content>div.manga-list>div.item>table>tbody>tr>td>a.cover>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "section.search>div.content>div.manga-list>div.item>table>tbody>tr>td>div.info.radius>p.field.last.summary"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "section.search>div.content>div.manga-list>div#paging-bar>ul.paging.full>li:contains(◀prev)>a"
    override var nextPageSelector: String = "section.search>div.content>div.manga-list>div#paging-bar>ul.paging.full>li:contains(next▶)>a"

    override fun generateSingleChoiceData(document: Document): Boolean {
        if (isFilterDataSingleChoiceFinalized) return true
        val singleValueFilterMutableMap = mutableMapOf<String, Map<String, String>>()
        for ((filterKey, filterSelectors) in dataSelectorsForSingleChoice) {
            try {
                val (labels, values) = parseLabelsValuesPair(
                        document = document,
                        filterSelectors = filterSelectors
                ).run {
                    if (filterKey in listOf("name-match", "autart-match", "orderby")) {
                        this
                    } else {
                        this.first.toMutableList().apply { add("") } to this.second.toMutableList().apply { add("") }
                    }
                }
                singleValueFilterMutableMap[filterKey] = labels.mapIndexed { index, label -> label to values[index] }.toMap()
            } catch (e: Exception) {
                return false
            }
        }
        filterBySingleChoice = singleValueFilterMutableMap
        isFilterDataSingleChoiceFinalized = true
        return true
    }

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
                ?: throw Exception("Failed to generate url")
        if (isRequestByGET) {
            userInput.forEach { urlBuilder.addQueryParameter(it.key, it.value) }
            multipleChoices
                    .map { it.first }
                    .distinct()
                    .forEach { key ->
                        (key to multipleChoices
                                .filter { (pairKey, _) -> pairKey == key }
                                .joinToString(separator = ",") { (_, pairValue) -> pairValue }).let {
                            urlBuilder.addQueryParameter(it.first, it.second)
                        }
                    }
            singleChoice.forEach { urlBuilder.addQueryParameter(it.key, it.value) }
        }
        return urlBuilder
                .apply {
                    if (isRequestByGET
                            && !(
                            previousPageSelector.isBlank()
                                    && nextPageSelector.isBlank()
                            )
                            && page > 1)
                        addQueryParameter("page", page.toString())
                }
                .build()
                .also { Timber.v(it.toString()) }
    }
}
