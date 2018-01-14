package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object TruyenMoiSegment : DomSegment {
    override var source: Source = TruyenTranh8Source
    override var pathName: String = "Truyện mới"
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

    override var urisSelector: String = "#truyenMoi div.item>div:nth-child(1)>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "#truyenMoi div.item>div:nth-child(2)>div.trInfo>h2"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "#truyenMoi div.item>div:nth-child(1)>a>img"
    override var thumbnailsAttribute: String = "data-src"
    override var descriptionsSelector: String = "#truyenMoi div.item>div:nth-child(2)>div.trInfo>p"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = ""
    override var nextPageSelector: String = ""
}