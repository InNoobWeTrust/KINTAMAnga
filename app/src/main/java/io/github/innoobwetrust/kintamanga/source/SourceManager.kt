package io.github.innoobwetrust.kintamanga.source

import android.net.Uri
import io.github.innoobwetrust.kintamanga.source.instance.en.mangafox.MangaFoxSource
import io.github.innoobwetrust.kintamanga.source.instance.en.mangahere.MangaHereSource
import io.github.innoobwetrust.kintamanga.source.instance.en.mangapark.MangaParkSource
import io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh.HocVienTruyenTranhSource
import io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8.TruyenTranh8Source
import io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan.TruyenTranhTuanSource
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object SourceManager {
    private var sources: List<Source> = listOf(
            HocVienTruyenTranhSource,
            TruyenTranhTuanSource,
            TruyenTranh8Source,
            MangaParkSource,
            MangaHereSource,
            MangaFoxSource
    )
    val sourceNameList: List<String> = sources.map { it.sourceName }.toList()
    val sourceNameListWithLang: List<String> = sources.map { "${it.sourceName} (${it.sourceLang})" }.toList()

    fun findFirstSourceNameForHost(host: String?): String? {
        if (null == host) return null
        return sources.find { it.rootUri.contains(host) || it.aliasRootUri.contains(host) }?.sourceName
    }

    fun normalizeUri(uri: Uri): String? {
        val sourceName = findFirstSourceNameForHost(host = uri.host) ?: return null
        val source = getSourceByName(sourceName = sourceName) ?: return null
        val uriString = uri.toString().let { if (it.isNotBlank() && it.last() != '/') "$it/" else it }
        return uriString.replaceFirst(source.aliasRootUri, source.rootUri)
    }

    fun getSourceByName(sourceName: String): Source? =
            sources.find { sourceName == it.sourceName }

    fun getMangaSegmentForSource(sourceName: String, segmentIndex: Int): SourceSegment? =
            getSourceByName(sourceName)?.mangaSegments?.getOrNull(segmentIndex)

    fun getMangaInfoProcessorForSourceName(sourceName: String): MangaInfoProcessor? =
            getSourceByName(sourceName)?.mangaInfoProcessor

    fun getChapterInfoProcessorForSourceName(sourceName: String): ChapterInfoProcessor? =
            getSourceByName(sourceName)?.chapterInfoProcessor
}
