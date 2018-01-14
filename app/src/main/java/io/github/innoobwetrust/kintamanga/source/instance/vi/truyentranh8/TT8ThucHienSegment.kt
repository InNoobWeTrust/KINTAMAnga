package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object TT8ThucHienSegment : DomSegment {
    override var source: Source = TruyenTranh8Source
    override var pathName: String = "TT8 phát hành"
    override var pathSegment: String = ""

    override var filterKeyLabel: Map<String, String> = emptyMap()
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = emptyMap()

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "#tt8ThucHien div.item>a.thumb"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "#tt8ThucHien div.item>a>h3"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "#tt8ThucHien div.item>a.thumb>img"
    override var thumbnailsAttribute: String = "data-src"
    override var descriptionsSelector: String = "#tt8ThucHien div.item>a.chap"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = ""
    override var nextPageSelector: String = ""
}