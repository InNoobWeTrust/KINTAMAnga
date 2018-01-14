package io.github.innoobwetrust.kintamanga.source.instance.en.mangafox

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
    override var source: Source = MangaFoxSource
    override var pathName: String = "Search"
    override var pathSegment: String = "search.php"

    override var filterKeyLabel: Map<String, String> = mapOf(
            // User input
            "name" to "Series Name",
            "author" to "Author Name",
            "artist" to "Artist Name",
            "released" to "Year of Released",
            // Single choice
            "name_method" to "Series matching strategy",
            "author_method" to "Author matching strategy",
            "artist_method" to "Artist matching strategy",
            "released_method" to "Year matching strategy",
            "type" to "Type",
            "rating" to "Rating",
            "rating_method" to "Rating matching strategy",
            "is_completed" to "Completed Series",
            "sort" to "Sort by",
            "order" to "Order by",
            // Multiple choices
            "genres" to "Genres",
            "genres-exclude" to "Exclude genres"
    )
    override var filterByUserInput: List<String> = listOf("name", "author", "artist", "released")
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
            "name_method" to listOf(
                    "#searchbar select[name=name_method]>option",
                    "text",
                    "#searchbar select[name=name_method]>option",
                    "value"
            ),
            "author_method" to listOf(
                    "#searcharea select[name=author_method]>option",
                    "text",
                    "#searcharea select[name=author_method]>option",
                    "value"
            ),
            "artist_method" to listOf(
                    "#searcharea select[name=artist_method]>option",
                    "text",
                    "#searcharea select[name=artist_method]>option",
                    "value"
            ),
            "released_method" to listOf(
                    "#searcharea select[name=released_method]>option",
                    "text",
                    "#searcharea select[name=released_method]>option",
                    "value"
            ),
            "type" to listOf(
                    "li.clear>label",
                    "text",
                    "li.clear>input",
                    "value"
            ),
            "rating" to listOf(
                    "#searcharea>ol>li:contains(Rating)>select[name=rating]>option",
                    "text",
                    "#searcharea>ol>li:contains(Rating)>select[name=rating]>option",
                    "value"
            ),
            "rating_method" to listOf(
                    "#searcharea>ol>li:contains(Rating)>select[name=rating_method]>option",
                    "text",
                    "#searcharea>ol>li:contains(Rating)>select[name=rating_method]>option",
                    "value"
            ),
            "is_completed" to listOf(
                    "#searcharea>ol>li:contains(Completed Series)>ul>li>label",
                    "text",
                    "#searcharea>ol>li:contains(Completed Series)>ul>li>input",
                    "value"
            )
    )
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = mapOf(
            "genres" to listOf(
                    "#genres>li>label>a",
                    "text",
                    "#genres>li>select.genres",
                    "name"
            ),
            "genres-exclude" to listOf(
                    "#genres>li>label>a",
                    "text",
                    "#genres>li>select.genres",
                    "name"
            )
    )
    override var filterRequiredDefaultUserInput: Map<String, String> = mapOf(
            "name" to "",
            "author" to "",
            "artist" to "",
            "released" to ""
    )
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "name_method" to "cw",
            "author_method" to "cw",
            "artist_method" to "cw",
            "released_method" to "eq",
            "type" to "",
            "rating" to "",
            "rating_method" to "eq",
            "is_completed" to "",
            "sort" to "name",
            "order" to "az",
            "advopts" to "1"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = false
    override var isFilterDataMultipleChoicesFinalized: Boolean = false
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "div#mangalist>ul.list>li>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "div#mangalist>ul.list>li>div.manga_text>a.title.series_preview.top"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "div#mangalist>ul.list>li>a>div>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "div#mangalist>ul.list>li>div.manga_text>p.nowrap.latest>a"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "div#nav>ul>li:contains(Prev)>a"
    override var nextPageSelector: String = "div#nav>ul>li:contains(Next)>a"

    private val listSortSingleChoices: Map<String, Map<String, String>> = mapOf(
            "sort" to mapOf(
                    "Name" to "name",
                    "Rating" to "rating",
                    "Views" to "views",
                    "Chapters" to "total_chapters",
                    "Latest Chapter" to "last_chapter_time"
            ),
            "order" to mapOf(
                    "A-Z" to "az",
                    "Z-A" to "za"
            )
    )

    override fun generateSingleChoiceData(document: Document): Boolean {
        if (isFilterDataSingleChoiceFinalized) return true
        val singleValueFilterMutableMap = mutableMapOf<String, Map<String, String>>()
        for ((filterKey, filterSelectors) in dataSelectorsForSingleChoice) {
            try {
                val (labels, values) = parseLabelsValuesPair(
                        document = document,
                        filterSelectors = filterSelectors
                )
                singleValueFilterMutableMap[filterKey] = labels.mapIndexed { index, label -> label to values[index] }.toMap()
                listSortSingleChoices.forEach { (key, labelValueMap) ->
                    singleValueFilterMutableMap[key] = labelValueMap
                }
            } catch (e: Exception) {
                return false
            }
        }
        filterBySingleChoice = singleValueFilterMutableMap
        isFilterDataSingleChoiceFinalized = true
        return true
    }

    @Throws(Exception::class)
    override fun buildURL(
            page: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>
    ): HttpUrl {
        if (page < 1) throw IllegalArgumentException(
                "${source.sourceName} - $pathSegment: invalid page number"
        )
        val urlBuilder: HttpUrl.Builder = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.addEncodedPathSegments(pathSegment)
                ?: throw Exception("${source.sourceName} - $pathSegment: Failed to generate url")
        if (isRequestByGET) {
            userInput.forEach { urlBuilder.addQueryParameter(it.key, it.value) }
            val multipleChoiceKeyValue =
                    multipleChoices
                            // Sort it so that "genres" will overwrite "genres-exclude"
                            .sortedBy { it.first }
                            // Remove inconsistency values
                            .distinctBy { it.second }
            val multipleChoiceValues = multipleChoiceKeyValue.map { it.second }
            filterByMultipleChoices["genres"]!!
                    .forEach { (_, value) ->
                        urlBuilder.addQueryParameter(
                                value,
                                when (value) {
                                // Default to 0 if not checked
                                    !in multipleChoiceValues -> "0"
                                    else -> when (multipleChoiceKeyValue.find {
                                        // Find matching genre and return its strategy
                                        it.second == value
                                    }?.first) {
                                        "genres" -> "1"
                                        "genres-exclude" -> "2"
                                        else -> "0"
                                    }
                                }
                        )
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
