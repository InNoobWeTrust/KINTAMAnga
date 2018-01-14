package io.github.innoobwetrust.kintamanga.source.instance.en.mangahere

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.model.SourceType
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor

object MangaHereSource : Source {
    override var sourceName: String = "Manga Here"
    override var sourceLang: String = "EN"
    override var rootUri: String = "http://www.mangahere.co/"
    override var aliasRootUri: String = "http://m.mangahere.co/"
    override val sourceType: SourceType = SourceType.DOM_SOURCE
    override var mangaSegments: List<SourceSegment> = listOf(
            MangaDirectorySegment,
            SearchSegment,
            NewMangaSegment,
            CompletedMangaSegment
    )
    override var teamSegments: List<SourceSegment> = emptyList()
    override var authorSegments: List<SourceSegment> = emptyList()
    override var artistSegments: List<SourceSegment> = emptyList()
    override var mangaInfoProcessor: MangaInfoProcessor = MangaHereMangaInfoProcessor
    override var chapterInfoProcessor: ChapterInfoProcessor = MangaHereChapterInfoProcessor
}