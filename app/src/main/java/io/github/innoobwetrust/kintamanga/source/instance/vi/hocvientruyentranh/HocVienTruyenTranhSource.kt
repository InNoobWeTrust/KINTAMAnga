package io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object HocVienTruyenTranhSource : Source {
    override var sourceName: String = "Học Viện Truyện Tranh"
    override var sourceLang: String = "VI"
    override var rootUri: String = "https://hocvientruyentranh.net"
    override var aliasRootUri: String = "https://hocvientruyentranh.net"
    override val sourceType: SourceType = SourceType.DOM_SOURCE

    override var mangaSegments: List<SourceSegment> = listOf(
            AllSegment,
            SearchSegment,
            NewChapterSegment,
            NewMangaSegment,
            MostViewedSegment,
            MostFollowedSegment,
            TopDaySegment,
            TopWeekSegment,
            TopMonthSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = HocVienTruyenTranhMangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = HocVienTruyenTranhChapterInfoProcessor
}
