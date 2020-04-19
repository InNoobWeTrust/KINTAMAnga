package io.github.innoobwetrust.kintamanga.source.dom.parser

import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import io.github.innoobwetrust.kintamanga.util.extension.parseUri
import org.jsoup.nodes.Document

interface DomSegmentParser {
    var source: Source

    var urisSelector: String
    var urisAttribute: String
    var titlesSelector: String
    var titlesAttribute: String
    var thumbnailsSelector: String
    var thumbnailsAttribute: String
    var descriptionsSelector: String
    var descriptionsAttribute: String
    var previousPageSelector: String
    var nextPageSelector: String

    fun elementInfosFromDocument(document: Document): List<ElementInfo> {
        val uris = document.parseListUri(
                selector = urisSelector,
                attribute = urisAttribute
        )
        val titles = document.parseListString(
                selector = titlesSelector,
                attribute = titlesAttribute
        )
        val thumbnails = document.parseListUri(
                selector = thumbnailsSelector,
                attribute = thumbnailsAttribute
        )
        val descriptions = document.parseListString(
                selector = descriptionsSelector,
                attribute = descriptionsAttribute
        )
        return uris.mapIndexed { index, s ->
            ElementInfo().apply {
                sourceName = source.sourceName
                itemUri = s
                itemTitle = titles.getOrElse(index) { "" }
                itemThumbnailUri = thumbnails.getOrElse(index) { "" }
                itemDescription = descriptions.getOrElse(index) { "" }
            }
        }
    }

    fun previousPageUriFromDocument(document: Document): String =
            if (!previousPageSelector.isBlank())
                document.parseUri(selector = previousPageSelector, attribute = "href")
            else ""

    fun nextPageUriFromDocument(document: Document): String =
            if (!nextPageSelector.isBlank())
                document.parseUri(selector = nextPageSelector, attribute = "href")
            else ""
}