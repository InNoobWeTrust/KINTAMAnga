package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.*

object MoiCapNhatSegment : DomSegment {
    override var source: Source = TruyenTranhTuanSource
    override var pathName: String = "Mới cập nhật"
    override var pathSegment: String = ""

    override var filterKeyLabel: Map<String, String> = emptyMap()
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = emptyMap()

    override var urisSelector: String = "div.manga-focus>span.manga.easy-tooltip>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "div.manga-focus>span.manga.easy-tooltip>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = ""
    override var thumbnailsAttribute: String = ""
    override var descriptionsSelector: String = ""
    override var descriptionsAttribute: String = ""
    override var previousPageSelector: String = "li.left-arrow>a"
    override var nextPageSelector: String = "li.right-arrow>a"

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

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
        val url = buildURL(page = pageNumber)
        val request: Request = buildGETRequest(url = url.toString(), forceNetwork = forceRefresh)
        return instance<OkHttpClient>().newCall(request).execute()
    }
}