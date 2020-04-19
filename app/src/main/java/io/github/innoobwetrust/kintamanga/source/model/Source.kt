package io.github.innoobwetrust.kintamanga.source.model

import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import java.io.Serializable

interface Source : Serializable {
    val sourceType: SourceType
    var sourceName: String
    var sourceLang: String
    var rootUri: String
    var aliasRootUri: String
    var mangaSegments: List<SourceSegment>
    var teamSegments: List<SourceSegment>
    var authorSegments: List<SourceSegment>
    var artistSegments: List<SourceSegment>
    var mangaInfoProcessor: MangaInfoProcessor
    var chapterInfoProcessor: ChapterInfoProcessor
}