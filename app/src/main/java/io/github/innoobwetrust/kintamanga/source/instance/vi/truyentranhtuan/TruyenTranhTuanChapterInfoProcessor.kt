package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.model.Chapter
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.util.extension.uriString
import okhttp3.CacheControl
import okhttp3.Headers
import timber.log.Timber
import java.util.*

object TruyenTranhTuanChapterInfoProcessor : ChapterInfoProcessor {
    override var source: Source = TruyenTranhTuanSource

    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    @Throws(Exception::class)
    override fun fetchPageList(chapter: Chapter): List<Page> {
        var extractedData: String? = null
        var extractedUrlData: String? = null
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
                            "var slides_page_path = \\[([^]]*)]",
                            0
                    ).let { data ->
                        if (data.isNullOrBlank()) return@let
                        extractedData = "var slides_page_path = \\[([^]]*)]"
                                .toRegex()
                                .find(data)
                                ?.groupValues
                                ?.get(1)
                    }
                    findWithinHorizon(
                            "var slides_page_url_path = \\[([^]]*)]",
                            0
                    ).let { data ->
                        if (data.isNullOrBlank()) return@let
                        extractedUrlData = "var slides_page_url_path = \\[([^]]*)]"
                                .toRegex()
                                .find(data)
                                ?.groupValues
                                ?.get(1)
                    }
                }
            }
        }
        if (null == extractedData && null == extractedUrlData)
            throw Exception("Neither slides_page_path or slides_page_url_path found!")
        Timber.v("slides_page_url_path: $extractedUrlData")
        Timber.v("slides_page_path: $extractedData")
        // Prefer free host over paid host to save bandwidth for source
        val imagesUriFirstServer: List<String> = extractedUrlData
                ?.split(regex = "[\"\\s]".toRegex())
                ?.let { urlData ->
                    val uris = urlData
                            .filterNot { it.isBlank() || "," == it }
                            .map { it.trim().uriString(response.request.url.toUrl()) }
                    if (uris.isNotEmpty())
                        return@let uris
                    else
                        return@let emptyList<String>()
                } ?: emptyList()
        val imagesUriSecondServer = extractedData
                ?.split("[\"\\s]".toRegex())
                ?.let { data ->
                    val uris = data
                            .filterNot { it.isBlank() || "," == it }
                            .sorted()
                            .map { it.trim().uriString(response.request.url.toUrl()) }
                    if (uris.isNotEmpty())
                        return@let uris
                    else
                        return@let emptyList<String>()
                } ?: emptyList()
        if (imagesUriFirstServer.isEmpty() && imagesUriSecondServer.isEmpty())
            return emptyList()
        return when {
            imagesUriSecondServer.isEmpty() -> imagesUriFirstServer
                    .mapIndexed { index, s ->
                        Timber.v(s)
                        Page(pageIndex = index, imageUrls = listOf(s))
                    }
            imagesUriFirstServer.isEmpty() -> imagesUriSecondServer
                    .mapIndexed { index, s ->
                        Timber.v(s)
                        Page(pageIndex = index, imageUrls = listOf(s))
                    }
            else -> imagesUriFirstServer.zip(imagesUriSecondServer)
                    .mapIndexed { index, s ->
                        Timber.v(s.toString())
                        Page(pageIndex = index, imageUrls = s.toList())
                    }
        }
    }
}
