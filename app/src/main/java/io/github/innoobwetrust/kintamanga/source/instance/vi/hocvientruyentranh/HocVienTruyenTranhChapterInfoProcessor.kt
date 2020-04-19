package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object HocVienTruyenTranhChapterInfoProcessor : DomChapterInfoProcessor {
    override var source: Source = HocVienTruyenTranhSource
    override var imagesUriSelector: String = ".manga-container>img"
    override var imagesUriAttribute: String = "src"

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()
}
