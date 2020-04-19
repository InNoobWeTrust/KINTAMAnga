package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranhtuan

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object TruyenTranhTuanSource : Source {
    override var sourceName: String = "Truyện Tranh Tuần"
    override var sourceLang: String = "VI"
    override var rootUri: String = "http://truyentranhtuan.com/"
    override var aliasRootUri: String = "http://truyentranhtuan.com/"
    override val sourceType: SourceType = SourceType.DOM_SOURCE_REGEX_CHAPTER
    override var mangaSegments: List<SourceSegment> = listOf(
            MoiCapNhatSegment, SearchSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = TruyenTranhTuanMangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = TruyenTranhTuanChapterInfoProcessor
}