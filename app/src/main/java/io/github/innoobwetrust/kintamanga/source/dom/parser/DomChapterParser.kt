package io.github.innoobwetrust.kintamanga.source.dom.parser

import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import org.jsoup.nodes.Document

interface DomChapterParser {
    var imagesUriSelector: String
    var imagesUriAttribute: String

    private fun imageUrisFromDocument(document: Document): List<String> = document.parseListUri(
            selector = imagesUriSelector,
            attribute = imagesUriAttribute
    )

    fun pagesFromDocument(document: Document): List<Page> =
            imageUrisFromDocument(document = document)
                    .mapIndexed { index, s -> Page(pageIndex = index, imageUrls = listOf(s)) }
}