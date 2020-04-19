package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomMangaInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object TruyenTranh8MangaInfoProcessor : DomMangaInfoProcessor {
    override var source: Source = TruyenTranh8Source

    override var titleSelector: String = "h1[itemprop=name]"
    override var titleAttribute: String = "text"
    override var thumbnailUriSelector: String = "img[itemprop=image]"
    override var thumbnailUriAttribute: String = "src"
    override var descriptionSelector: String = "div[itemprop=description]"
    override var descriptionAttribute: String = "text"

    override var alternativeTitleSelector: String = ".mangainfo>li:contains(Tên khác:)>a"
    override var alternativeTitleAttribute: String = "text"
    override var typesSelector: String = ""
    override var typesAttribute: String = ""
    override var genresSelector: String = ".mangainfo>li:contains(Thể loại:)>a"
    override var genresAttribute: String = "text"
    override var statusSelector: String = ".mangainfo>li:contains(Tình Trạng:)>a"
    override var statusAttribute: String = "text"
    override var authorsSelector: String = ".mangainfo>li:contains(Tác giả:)>a"
    override var authorsAttribute: String = "text"
    override var artistsSelector: String = ""
    override var artistAttribute: String = ""
    override var teamsSelector: String = ".mangainfo>li:contains(Dịch:)>a"
    override var teamAttribute: String = "text"
    override var warningSelector: String = ""
    override var warningAttribute: String = ""
    override var chaptersUriSelector: String = "a[itemprop=itemListElement]"
    override var chaptersUriAttribute: String = "href"
    override var chaptersTitleSelector: String = "a[itemprop=itemListElement]"
    override var chaptersTitleAttribute: String = "title"
    override var chaptersDescriptionSelector: String = ""
    override var chaptersDescriptionAttribute: String = ""
    override var chaptersUpdateTimeSelector: String = "a[itemprop=itemListElement] time"
    override var chaptersUpdateTimeAttribute: String = "datetime"
    override var chaptersIndexedDescending: Boolean = true

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun warningFromDocument(document: Document): String {
        val warningRegex = "var sRating = \"18\";".toRegex()
        val warningScript = document
                .getElementsByTag("script")
                .find { null != warningRegex.find(it.data()) }
        return if (null == warningScript) ""
        else "Truyện bạn sắp xem có những nội dung nhạy cảm, chỉ phù hợp lứa tuổi 18 trở lên. Hãy cân nhắc khi tiếp tục.\n" +
                "\n" +
                "Tại trang này, chúng tôi từ chối hoàn toàn mọi ảnh hưởng, quy chế, pháp luật đến bạn và đến chúng tôi.\n" +
                "\n" +
                "Nếu làm ảnh hưởng đến cá nhân hay tổ chức nào, khi được yêu cầu, chúng tôi sẽ xem xét và gỡ bỏ. Chúc bạn có những giây phút thoải mái."
    }
}