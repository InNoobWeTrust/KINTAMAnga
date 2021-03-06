package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object NewMangaSegment: DomSegment {
    override var source: Source = HocVienTruyenTranhSource
    override var pathName: String = "New manga"
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

    override var urisSelector: String = "#new-manga li>div.manga-box>a.__image"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "#new-manga li>div.manga-box>div.__info>h4.__title>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "#new-manga li>div.manga-box>a.__image>img"
    override var thumbnailsAttribute: String = "data-src"
    override var descriptionsSelector: String = "#new-manga li>div.manga-box>div.__info"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = ""
    override var nextPageSelector: String = ""
}
