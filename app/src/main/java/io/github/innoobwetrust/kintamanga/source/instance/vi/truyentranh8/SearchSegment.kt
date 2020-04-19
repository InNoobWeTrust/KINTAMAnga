package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseUri
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object SearchSegment : DomSegment {
    override var source: Source = TruyenTranh8Source
    override var pathName: String = "Tìm Truyện"
    override var pathSegment: String = "search.php"

    override var filterKeyLabel: Map<String, String> = mapOf(
            "q" to "Nhập tên truyện",
            "TacGia" to "Tên tác giả",
            "Nguon" to "Nguồn/Nhóm dịch",
            "NamPhaHanh" to "Năm phát hành",
            "u" to "Đăng bởi thành viên",
            "danhcho" to "Dành cho",
            "DoTuoi" to "Độ tuổi",
            "TinhTrang" to "Tình trạng",
            "quocgia" to "Quốc gia",
            "KieuDoc" to "Kiểu đọc",
            "baogom" to "Thể loại"
    )
    override var filterByUserInput: List<String> = listOf("q", "TacGia", "Nguon", "NamPhaHanh", "u")
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
            "danhcho" to listOf("#danhcho>option", "text", "#danhcho>option", "value"),
            "DoTuoi" to listOf("#DoTuoi>option", "text", "#DoTuoi>option", "value"),
            "TinhTrang" to listOf("#TinhTrang>option", "text", "#TinhTrang>option", "value"),
            "quocgia" to listOf("#quocgia>option", "text", "#quocgia>option", "value"),
            "KieuDoc" to listOf("#KieuDoc>option", "text", "#KieuDoc>option", "value")
    )
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = mapOf(
            "baogom" to listOf("#chontheloai>ul>li", "text", "#chontheloai>ul>li", "data-id")
    )
    override var filterRequiredDefaultUserInput: Map<String, String> = mapOf(
            "q" to ""
    )
    override var filterRequiredDefaultSingleChoice: Map<String, String> = mapOf(
            "danhcho" to "",
            "DoTuoi" to "",
            "TinhTrang" to "",
            "quocgia" to "",
            "KieuDoc" to "",
            "sort" to "ten",
            "view" to "list",
            "act" to "timnangcao"
    )

    override var isFilterDataSingleChoiceFinalized = false
    override var isFilterDataMultipleChoicesFinalized = false
    override var isUsable = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "#blocklist.table.table-hover>tbody>tr>td.tit>a.tipsy"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "#blocklist.table.table-hover>tbody>tr>td.tit>a.tipsy"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "#blocklist.table.table-hover>tbody>tr>td.tit>a.tipsy"
    override var thumbnailsAttribute: String = "title"
    override var descriptionsSelector: String = "#blocklist.table.table-hover>tbody>tr>td.tit>a.tipsy"
    override var descriptionsAttribute: String = "title"
    override var previousPageSelector: String = "p.page>a:contains(«)"
    override var nextPageSelector: String = "p.page>a:contains(»)"

    override fun elementInfosFromDocument(document: Document): List<ElementInfo> {
        val uris = document.parseListString(
                selector = urisSelector,
                attribute = urisAttribute
        )
        val titles = document.parseListString(
                selector = titlesSelector,
                attribute = titlesAttribute
        )
        val thumbnails = document.parseListString(
                selector = thumbnailsSelector,
                attribute = thumbnailsAttribute
        )
                .map { "<img src=.+?>".toRegex().find(it)?.value ?: "" }
                .map { Jsoup.parse(it).parseUri(selector = "img", attribute = "src") }
        val descriptions = document.parseListString(
                selector = descriptionsSelector,
                attribute = descriptionsAttribute
        )
                .map { "<img src=.+>(.*)".toRegex().find(it)?.groupValues?.get(1) ?: "" }
        return uris.mapIndexed { index, s ->
            ElementInfo().apply {
                sourceName = source.sourceName
                itemUri = s
                itemTitle = titles.getOrElse(index) { "" }
                itemThumbnailUri = thumbnails.getOrElse(index) { "" }
                itemDescription = descriptions.getOrElse(index) { "" }
            }
        }
    }

    override fun fetchFilterData(): Boolean {
        // Check invalid filter
        if (!validateFilterSelector()) return false
        val url = source.rootUri.toHttpUrlOrNull()
                ?.newBuilder()
                ?.addEncodedPathSegments("search")
                ?.build() ?: return false
        val request = GET(
                url = url.toString(),
                headers = headers(),
                cacheControl = cacheControl()
        )
        val document = try {
            instance<OkHttpClient>().newCall(request).execute().asJsoupDocument()
        } catch (e: Exception) {
            Document(request.url.toString())
        }
        if (!document.hasText()) return false
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
}
