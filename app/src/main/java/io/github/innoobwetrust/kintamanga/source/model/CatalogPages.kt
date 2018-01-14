package io.github.innoobwetrust.kintamanga.source.model

import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import java.io.Serializable

class CatalogPages : Serializable {
    var isReady: Boolean = false
        private set
    private lateinit var catalogPage: CatalogPage
    var elementInfos: ArrayList<ElementInfo> = arrayListOf()
        private set

    // Can reset multiple times
    fun setup(catalogPage: CatalogPage): Boolean {
        this.catalogPage = catalogPage
        elementInfos.clear()
        elementInfos.addAll(catalogPage.elementInfos)
        this.isReady = true
        return true
    }

    fun dataSetup(elementInfos: List<ElementInfo>): Boolean {
        this.elementInfos.clear()
        this.elementInfos.addAll(elementInfos)
        return true
    }

    fun hasNextPage(): Boolean = catalogPage.nextPageUri.isNotEmpty()

    fun appendNextCatalogPage(catalogPage: CatalogPage): Boolean {
        if (catalogPage.pageNumber == this.catalogPage.pageNumber) return false
        if (catalogPage.elementInfos.isEmpty()) return false
        this.catalogPage = catalogPage
        elementInfos.addAll(catalogPage.elementInfos)
        return true
    }

    fun loadNext(): CatalogPage? = catalogPage.nextCatalogPage()

}
