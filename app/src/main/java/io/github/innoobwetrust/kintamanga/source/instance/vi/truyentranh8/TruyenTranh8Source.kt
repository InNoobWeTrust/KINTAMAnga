package io.github.innoobwetrust.kintamanga.source.instance.vi.truyentranh8

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object TruyenTranh8Source : Source {
    override var sourceName: String = "Truyện Tranh 8"
    override var sourceLang: String = "VI"
    override var rootUri: String = "http://truyentranh8.net/"
    override var aliasRootUri: String = "http://m.truyentranh8.net/"
    override val sourceType: SourceType = SourceType.DOM_SOURCE_JS_CHAPTER
    override var mangaSegments: List<SourceSegment> = listOf(
            ChapMoiSegment,
            SearchSegment,
            TruyenMoiSegment,
            SieuPhamSegment,
            TT8ThucHienSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = TruyenTranh8MangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = TruyenTranh8ChapterInfoProcessor
}