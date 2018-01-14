package io.github.innoobwetrust.kintamanga.source.instance.en.mangahere

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import timber.log.Timber

object SearchSegment : DomSegment {
    override var source: Source = MangaHereSource
    override var pathName: String = "Search"
    override var pathSegment: String = "search.php"
    private var filterPathSegment: String = "advsearch.htm"

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
            "direction" to "Type",
            "is_completed" to "Completed Series",
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
                    "#searchform>ul>li:contains(Series Name)>select>option",
                    "text",
                    "#searchform>ul>li:contains(Series Name)>select>option",
                    "value"
            ),
            "author_method" to listOf(
                    "#searchform>ul>li:contains(Author Name)>select>option",
                    "text",
                    "#searchform>ul>li:contains(Author Name)>select>option",
                    "value"
            ),
            "artist_method" to listOf(
                    "#searchform>ul>li:contains(Artist Name)>select>option",
                    "text",
                    "#searchform>ul>li:contains(Artist Name)>select>option",
                    "value"
            ),
            "released_method" to listOf(
                    "#searchform>ul>li:contains(Year of Released)>select>option",
                    "text",
                    "#searchform>ul>li:contains(Year of Released)>select>option",
                    "value"
            ),
            "is_completed" to listOf(
                    "#searchform>ul>li:contains(Completed Series)>input[type=radio]",
                    "text",
                    "#searchform>ul>li:contains(Completed Series)>span",
                    "value"
            )
    )
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = mapOf(
            "genres" to listOf(
                    "#genres>li>a",
                    "text",
                    "#genres>li>select",
                    "name"
            ),
            "genres-exclude" to listOf(
                    "#genres>li>a",
                    "text",
                    "#genres>li>select",
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
            "direction" to "",
            "is_completed" to "",
            "advopts" to "1"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = false
    override var isFilterDataMultipleChoicesFinalized: Boolean = false
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "body>section>article>div>div.result_search>dl>dt>a.manga_info.name_one"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "body>section>article>div>div.result_search>dl>dt>a.manga_info.name_one"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = ""
    override var thumbnailsAttribute: String = ""
    override var descriptionsSelector: String = "body>section>article>div>div.result_search>dl>dt>a.manga_info.name_two"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = "body>section>article>div>div.result_search>div.directory_footer>div>a.prew"
    override var nextPageSelector: String = "body>section>article>div>div.result_search>div.directory_footer>div>a.next"

    private val listTypeSingleChoices: Map<String, Map<String, String>> = mapOf(
            "direction" to mapOf(
                    "Japanese Manga (read from right to left)" to "rl",
                    "Korean Manhwa (read from left to right)" to "lr",
                    "Either" to ""
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
                listTypeSingleChoices.forEach { (key, labelValueMap) ->
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

    override fun fetchFilterData(): Boolean {
        // Check invalid filter
        if (!validateFilterSelector()) return false
        val url = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.apply {
                    if (this@SearchSegment.filterPathSegment.isNotEmpty())
                        addEncodedPathSegments(this@SearchSegment.filterPathSegment)
                }
                ?.build() ?: return false
        val request = GET(
                url = url.toString(),
                headers = headers(),
                cacheControl = cacheControl()
        )
        val document = try {
            instance<OkHttpClient>().newCall(request).execute().asJsoupDocument()
        } catch (e: Exception) {
            Document(request.url.toString())
        }
        if (!document.hasText()) return false
        try {
            generateSingleChoiceData(document = document)
        } catch (e: Exception) {
            return false
        }
        try {
            generateMultipleChoicesData(document = document)
        } catch (e: Exception) {
            return false
        }
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
