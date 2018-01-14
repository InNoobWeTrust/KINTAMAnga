package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import com.github.salomonbrys.kodein.instance
import com.squareup.duktape.Duktape
import io.github.innoobwetrust.kintamanga.model.Chapter
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.util.extension.uriString
import okhttp3.CacheControl
import okhttp3.Headers
import timber.log.Timber
import java.util.*

object TruyenTranh8ChapterInfoProcessor : ChapterInfoProcessor {
    override var source: Source = TruyenTranh8Source

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    @Throws(Exception::class)
    override fun fetchPageList(chapter: Chapter): List<Page> {
        val response = fetchData(
                uri = chapter.chapterUri,
                headers = headers(),
                cacheControl = cacheControl()
        )
        response.body?.use { body ->
            body.source().inputStream().use { inputStream ->
                Scanner(
                        inputStream,
                        body.contentType()?.charset()?.name() ?: Charsets.UTF_8.name()
                ).run {
                    findWithinHorizon(
                            "eval\\(function\\(p,a,c,k,e,d\\).+",
                            0
                    ).let { data ->
                        if (data.isNullOrBlank()) return@let
                        // Create Javascript context to evaluate the data
                        var lstImages: List<String> = emptyList()
                        var lstImagesVip: List<String> = emptyList()
                        Duktape.create().use {
                            it.evaluate(data)
                            lstImages = it.evaluate("lstImages.toString();")
                                    .toString()
                                    .split(',')
                                    .map { urlStr -> urlStr.uriString(response.request.url.toUrl()) }
                                    .toList()
                            lstImagesVip = it.evaluate("lstImagesVIP.toString();")
                                    .toString()
                                    .split(',')
                                    .map { urlStr -> urlStr.uriString(response.request.url.toUrl()) }
                                    .toList()
                        }
                        if (lstImages.isEmpty() && lstImagesVip.isEmpty())
                            return emptyList()
                        return when {
                            lstImagesVip.isEmpty() -> lstImages
                                    .mapIndexed { index, s ->
                                        Timber.v(s)
                                        Page(pageIndex = index, imageUrls = listOf(s))
                                    }
                            lstImages.isEmpty() -> lstImagesVip
                                    .mapIndexed { index, s ->
                                        Timber.v(s)
                                        Page(pageIndex = index, imageUrls = listOf(s))
                                    }
                            else -> lstImages.zip(lstImagesVip)
                                    .mapIndexed { index, s ->
                                        Timber.v(s.toString())
                                        Page(pageIndex = index, imageUrls = s.toList())
                                    }
                        }
                    }
                }
            }
        }
        // If this point is reached, this means no matching data found
        return emptyList()
    }
}
