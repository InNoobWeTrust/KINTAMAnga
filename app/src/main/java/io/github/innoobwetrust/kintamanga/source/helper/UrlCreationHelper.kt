package io.github.innoobwetrust.kintamanga.source.helper

import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

interface UrlCreationHelper {
    var source: Source
    var pathSegment: String

    var isRequestByGET: Boolean

    var previousPageSelector: String
    var nextPageSelector: String

    @Throws(Exception::class)
    fun buildURL(
            page: Int,
            userInput: Map<String, String> = emptyMap(),
            singleChoice: Map<String, String> = emptyMap(),
            multipleChoices: Set<Pair<String, String>> = emptySet()
    ): HttpUrl {
        if (page < 1) throw IllegalArgumentException(
                "${source.sourceName} - $pathSegment: invalid page number"
        )
        val urlBuilder: HttpUrl.Builder = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.apply {
                    if (pathSegment.isNotEmpty())
                        addEncodedPathSegments(pathSegment)
                } ?: throw Exception("${source.sourceName} - $pathSegment: Failed to generate url")
        if (isRequestByGET) {
            userInput.forEach { urlBuilder.addQueryParameter(it.key, it.value) }
            multipleChoices.forEach { urlBuilder.addQueryParameter(it.first, it.second) }
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
    }
}
