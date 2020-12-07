package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object AllSegment : DomSegment {
    override var source: Source = HocVienTruyenTranhSource
    override var pathName: String = "All"
    override var pathSegment: String = "truyen\\all"

    override var filterKeyLabel: Map<String, String> = mapOf("filter_type" to "Lọc theo")
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = mapOf(
            "filter_type" to mapOf(
                    "name" to "name",
                    "view" to "view",
                    "latest-chapter" to "latest-chapter",
                    "latest-manga" to "latest-manga"
            )
    )
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "filter_type" to "latest-chapter"
    )

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance<Headers>().newBuilder().add("Referer", source.rootUri).build()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = ".table.table-hover>tbody>tr>td:nth-child(1)>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = ".table.table-hover>tbody>tr>td:nth-child(1)>a"
    override var titlesAttribute: String = "title"
    override var thumbnailsSelector: String = ".table.table-hover>tbody>tr>td:nth-child(1)>a"
    override var thumbnailsAttribute: String = "data-thumbnail"
    override var descriptionsSelector: String = ".table.table-hover>tbody>tr>td:nth-child(1)>a"
    override var descriptionsAttribute: String = "data-description"
    override var previousPageSelector: String = ".pagination.no-margin>li>a[rel=prev]"
    override var nextPageSelector: String = ".pagination.no-margin>li>a[rel=next]"
}
