package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object SearchSegment : DomSegment {
    override var source: Source = HocVienTruyenTranhSource
    override var pathName: String = "Search"
    override var pathSegment: String = "searchs"

    override var filterKeyLabel: Map<String, String> = mapOf(
            "keyword" to "Từ khoá",
            "author" to "Tác giả",
            "type" to "Loại",
            "status" to "Tình trạng",
            "genres[]" to "Thể loại"
    )
    override var filterByUserInput: List<String> = listOf("keyword")
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
        get() {
            if (!isFilterDataSingleChoiceFinalized) {
                fetchFilterData()
            }
            return field
        }
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
        get() {
            if (!isFilterDataMultipleChoicesFinalized) {
                fetchFilterData()
            }
            return field
        }
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = mapOf(
            "author" to listOf("#author>option", "text", "#author>option", "value"),
            "type" to listOf("#type>option", "text", "#type>option", "value"),
            "status" to listOf("#status>option", "text", "#status>option", "value")
    )
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = mapOf(
            "genres[]" to listOf(".genre-item label", "text", ".genre-item input", "value")
    )
    override var filterRequiredDefaultUserInput: Map<String, String> = mapOf(
            "keyword" to ""
    )
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "type" to "-1", "author" to "-1", "status" to "-1", "submit" to "Tìm kiếm"
    )

    override var isFilterDataSingleChoiceFinalized = false
    override var isFilterDataMultipleChoicesFinalized = false
    override var isUsable = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
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
