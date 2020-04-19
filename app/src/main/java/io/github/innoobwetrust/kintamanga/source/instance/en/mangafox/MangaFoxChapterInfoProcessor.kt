package io.github.innoobwetrust.kintamanga.source.instance.en.mangafox

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.dom.DomChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response

object MangaFoxChapterInfoProcessor : DomChapterInfoProcessor {
    override var source: Source = MangaFoxSource
    override var imagesUriSelector: String = "div.mangaread-img>img"
    override var imagesUriAttribute: String = "data-original"

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override fun fetchData(
            uri: String,
            headers: Headers,
            cacheControl: CacheControl
    ): Response {
        // Alter the uri to get all images at once
        val request = GET(
                url = uri.replaceFirst(
                        oldValue = "http://mangafox.me/manga/",
                        newValue = "http://m.mangafox.me/roll_manga/"
                ).replace("/[0-9]+.html".toRegex(), ""),
                headers = headers,
                cacheControl = cacheControl
        )
        return instance<OkHttpClient>().newCall(request).execute()
    }
}