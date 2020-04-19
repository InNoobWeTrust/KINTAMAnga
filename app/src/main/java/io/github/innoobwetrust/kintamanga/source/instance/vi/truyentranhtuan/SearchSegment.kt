package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan

import com.github.salomonbrys.kodein.instance
import com.squareup.duktape.Duktape
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.CatalogPage
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.util.extension.uriString
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.*

object SearchSegment : DomSegment {
    override var source: Source = TruyenTranhTuanSource
    override var pathName: String = "Tìm kiếm"
    override var pathSegment: String = "wp-content\\themes\\nos\\js\\search.js "

    override var filterKeyLabel: Map<String, String> = emptyMap()
    override var filterByUserInput: List<String> = listOf("Tên truyện")
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = mapOf(
            "Tên truyện" to ""
    )
    override var filterRequiredDefaultSingleChoice: Map<String, String> = emptyMap()

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var urisSelector: String = "div.manga-focus>span.manga>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "div.manga-focus>span.manga>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = ""
    override var thumbnailsAttribute: String = ""
    override var descriptionsSelector: String = ""
    override var descriptionsAttribute: String = ""
    override var previousPageSelector: String = ""

    override var nextPageSelector: String = ""

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun fetchFilterData(): Boolean = true

    override fun fetchData(
            pageNumber: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>,
            forceRefresh: Boolean
    ): Response {
        if (source.rootUri.isBlank()) throw Exception("Empty rootUri, check source config!")
        searchString = userInput["Tên truyện"] ?: ""
        val url = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.apply {
                    if (pathSegment.isNotEmpty())
                        addEncodedPathSegments(pathSegment)
                }
                ?.build() ?: throw Exception("${source.sourceName} - $pathSegment: Failed to generate url")
        val request: Request = buildGETRequest(url = url.toString(), forceNetwork = forceRefresh)
        return instance<OkHttpClient>().newCall(request).execute()
    }

    private var searchString: String = ""

    override fun catalogPageFromResponse(response: Response, pageNumber: Int): CatalogPage {
        var pathItems: List<ElementInfo> = emptyList()
        Duktape.create().apply {
            evaluate(response.body?.string())
            val labels: List<String> = try {
                evaluate("availableTags.map(function(tag) {return tag.label}).toString();")
                        .toString()
                        .split(',')
                        .toList()
            } catch (e: Exception) {
                emptyList()
            }
            val urls: List<String> = try {
                evaluate("availableTags.map(function(tag) {return tag.url}).toString();")
                        .toString()
                        .split(',')
                        .map { urlStr -> urlStr.uriString(response.request.url.toUrl()) }
                        .toList()
            } catch (e: Exception) {
                emptyList()
            }
            pathItems = labels.zip(urls)
                    .map {
                        ElementInfo().apply {
                            sourceName = this@SearchSegment.source.sourceName
                            itemUri = it.second
                            itemTitle = it.first
                        }
                    }
                    .filter {
                        if (searchString.isNotBlank())
                            null != searchString.toLowerCase(Locale.ROOT).toRegex().find(it.itemTitle.toLowerCase(Locale.ROOT))
                        else
                            true
                    }
        }
        val previousPageUri = ""
        val nextPageUri = ""
        return CatalogPage(
                pageNumber = pageNumber,
                elementInfos = pathItems,
                previousPageUri = previousPageUri,
                nextPageUri = nextPageUri,
                sourceSegment = this
        )
    }
}
