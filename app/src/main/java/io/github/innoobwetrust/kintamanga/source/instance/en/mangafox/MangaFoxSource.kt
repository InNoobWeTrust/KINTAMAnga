package io.github.innoobwetrust.kintamanga.source.instance.en.mangafox

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object MangaFoxSource : Source {
    override var sourceName: String = "Manga Fox"
    override var sourceLang: String = "EN"
    override var rootUri: String = "http://mangafox.me/"
    override var aliasRootUri: String = "http://m.mangafox.me/"
    override val sourceType: SourceType = SourceType.DOM_SOURCE
    override var mangaSegments: List<SourceSegment> = listOf(
            MangaDirectorySegment,
            SearchSegment,
            NewMangaSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = MangaFoxMangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = MangaFoxChapterInfoProcessor
}