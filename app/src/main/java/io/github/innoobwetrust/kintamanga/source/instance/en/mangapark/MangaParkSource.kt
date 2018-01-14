package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object MangaParkSource : Source {
    override var sourceName: String = "Manga Park"
    override var sourceLang: String = "EN"
    override var rootUri: String = "http://mangapark.me/"
    override var aliasRootUri: String = "http://mangapark.me/"
    override val sourceType: SourceType = SourceType.DOM_SOURCE
    override var mangaSegments: List<SourceSegment> = listOf(
            AllMangaSegment,
            PopularMangaUpdatesSegment,
            SearchSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = MangaParkMangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = MangaParkChapterInfoProcessor
}