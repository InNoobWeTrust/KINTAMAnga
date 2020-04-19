package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers

object MangaParkChapterInfoProcessor : DomChapterInfoProcessor {
    override var source: Source = MangaParkSource
    override var imagesUriSelector: String = "section#viewer>div>a>img"
    override var imagesUriAttribute: String = "src"

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()
}