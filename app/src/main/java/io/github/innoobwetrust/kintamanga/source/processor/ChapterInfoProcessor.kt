package io.github.innoobwetrust.kintamanga.source.processor

import io.github.innoobwetrust.kintamanga.model.Chapter
import io.github.innoobwetrust.kintamanga.model.Page

interface ChapterInfoProcessor : BaseInfoProcessor {
    @Throws(Exception::class)
    fun fetchPageList(chapter: Chapter): List<Page>
}