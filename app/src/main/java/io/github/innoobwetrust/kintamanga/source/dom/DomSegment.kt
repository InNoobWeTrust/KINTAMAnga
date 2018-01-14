package io.github.innoobwetrust.kintamanga.source.dom

import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.dom.parser.DomSegmentParser
import io.github.innoobwetrust.kintamanga.source.helper.FilterHelper
import io.github.innoobwetrust.kintamanga.source.helper.RequestDataHelper
import io.github.innoobwetrust.kintamanga.source.helper.RequestHelper
import io.github.innoobwetrust.kintamanga.source.helper.UrlCreationHelper
import io.github.innoobwetrust.kintamanga.source.model.CatalogPage
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

interface DomSegment :
        SourceSegment,
        FilterHelper,
        UrlCreationHelper,
        RequestDataHelper,
        RequestHelper,
        DomSegmentParser,
        KodeinGlobalAware {

    // Fetch filter data
    override fun fetchFilterData(): Boolean {
        // Check invalid filter
        if (!validateFilterSelector()) return false
        val url = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.apply {
                    if (this@DomSegment.pathSegment.isNotEmpty())
                        addEncodedPathSegments(this@DomSegment.pathSegment)
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
            return false
        }
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
    override fun fetchData(
            pageNumber: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>,
            forceRefresh: Boolean
    ): Response {
        if (source.rootUri.isBlank()) throw Exception("${source.sourceName} - $pathSegment: Empty rootUri, check source config!")
        if (!validateFilterSelector()) throw Exception("${source.sourceName} - $pathSegment: filter's selectors validation failed")
        if (!isFilterDataSingleChoiceFinalized || !isFilterDataMultipleChoicesFinalized) {
            if (!fetchFilterData()) throw Exception("${source.sourceName} - $pathSegment: filter fetching failed")
        }
        val formattedRequestData = generateRequestData(
                userInput = userInput,
                singleChoice = singleChoice
        ) ?: throw Exception(
                "${source.sourceName} - $pathSegment: Error generating request data!"
        )
        val url = buildURL(
                page = pageNumber,
                userInput = formattedRequestData.first,
                singleChoice = formattedRequestData.second,
                multipleChoices = multipleChoices
        )
        val request: Request
        request = if (isRequestByGET) {
            buildGETRequest(url = url.toString(), forceNetwork = forceRefresh)
        } else {
            buildPOSTRequest(
                    url = url.toString(),
                    page = pageNumber,
                    userInput = userInput,
                    singleChoice = singleChoice,
                    multipleChoices = multipleChoices,
                    forceNetwork = forceRefresh
            )
        }
        return instance<OkHttpClient>().newCall(request).execute()
    }

    override fun fetchData(uri: String): Response = instance<OkHttpClient>()
            .newCall(GET(uri, headers(), cacheControl()))
            .execute()

    override fun catalogPageFromResponse(response: Response, pageNumber: Int): CatalogPage {
        val document = response.asJsoupDocument()
        val pathItems = elementInfosFromDocument(document)
        val previousPageUri = previousPageUriFromDocument(document)
        val nextPageUri = nextPageUriFromDocument(document)
        return CatalogPage(
                pageNumber = pageNumber,
                elementInfos = pathItems,
                previousPageUri = previousPageUri,
                nextPageUri = nextPageUri,
                sourceSegment = this
        )
    }
}
