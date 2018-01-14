package io.github.innoobwetrust.kintamanga.source.model

import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import java.io.Serializable

class CatalogPage(
        @JvmField val pageNumber: Int = 0,
        @JvmField val elementInfos: List<ElementInfo> = emptyList(),
        @JvmField val previousPageUri: String = "",
        @JvmField val nextPageUri: String = "",
        private val sourceSegment: SourceSegment
) : Serializable {
    fun previousCatalogPage(): CatalogPage? {
        if (1 == pageNumber) return null
        if (previousPageUri.isBlank()) return null
        val response = sourceSegment.fetchData(previousPageUri)
        return sourceSegment.catalogPageFromResponse(response = response, pageNumber = pageNumber - 1)
    }

    fun nextCatalogPage(): CatalogPage? {
        if (nextPageUri.isBlank()) return null
        val response = sourceSegment.fetchData(nextPageUri)
        return sourceSegment.catalogPageFromResponse(response = response, pageNumber = pageNumber + 1)
    }
}