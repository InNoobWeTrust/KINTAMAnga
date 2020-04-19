package io.github.innoobwetrust.kintamanga.source.instance.en.mangahere

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.dom.DomChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response

object MangaHereChapterInfoProcessor : DomChapterInfoProcessor {
    override var source: Source = MangaHereSource
    override var imagesUriSelector: String = "#viewer>img"
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
                        oldValue = "http://www.mangahere.co/manga/",
                        newValue = "http://m.mangahere.co/roll_manga/"
                ),
                headers = headers,
                cacheControl = cacheControl
        )
        return instance<OkHttpClient>().newCall(request).execute()
    }
}