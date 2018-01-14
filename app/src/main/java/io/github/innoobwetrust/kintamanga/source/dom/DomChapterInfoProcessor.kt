package io.github.innoobwetrust.kintamanga.source.dom

import io.github.innoobwetrust.kintamanga.model.Chapter
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.source.dom.parser.DomChapterParser
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument

interface DomChapterInfoProcessor : ChapterInfoProcessor, DomChapterParser {
    @Throws(Exception::class)
    override fun fetchPageList(chapter: Chapter): List<Page> = fetchData(
            uri = chapter.chapterUri,
            headers = headers(),
            cacheControl = cacheControl()
    ).asJsoupDocument().run(this::pagesFromDocument)
}