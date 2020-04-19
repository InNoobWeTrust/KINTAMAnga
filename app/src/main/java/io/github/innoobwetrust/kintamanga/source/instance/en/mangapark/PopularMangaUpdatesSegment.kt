package io.github.innoobwetrust.kintamanga.source.instance.en.mangapark

import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.source.dom.DomSegment
import io.github.innoobwetrust.kintamanga.source.model.Source
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.util.extension.attrOrText
import io.github.innoobwetrust.kintamanga.util.extension.parseElements
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseListUri
import okhttp3.CacheControl
import okhttp3.Headers
import org.jsoup.nodes.Document

object PopularMangaUpdatesSegment : DomSegment {
    override var source: Source = MangaParkSource
    override var pathName: String = "Popular Manga Updates"
    override var pathSegment: String = ""

    override var filterKeyLabel: Map<String, String> = emptyMap()
    override var filterByUserInput: List<String> = emptyList()
    override var filterBySingleChoice: Map<String, Map<String, String>> = emptyMap()
    override var filterByMultipleChoices: Map<String, Map<String, String>> = emptyMap()
    override var dataSelectorsForSingleChoice: Map<String, List<String>> = emptyMap()
    override var dataSelectorsForMultipleChoices: Map<String, List<String>> = emptyMap()
    override var filterRequiredDefaultUserInput: Map<String, String> = emptyMap()
    override var filterRequiredDefaultSingleChoice: Map<String, String> = emptyMap()

    override var isFilterDataSingleChoiceFinalized: Boolean = true
    override var isFilterDataMultipleChoicesFinalized: Boolean = true
    override var isUsable: Boolean = true

    override var isRequestByGET: Boolean = true
    override fun headers(): Headers = instance()
    override fun cacheControl(): CacheControl = instance()

    override var urisSelector: String = "ul.screen.thm-list>li>a"
    override var urisAttribute: String = "href"
    override var titlesSelector: String = "ul.screen.thm-list>li>h3>a"
    override var titlesAttribute: String = "text"
    override var thumbnailsSelector: String = "ul.screen.thm-list>li>a>img"
    override var thumbnailsAttribute: String = "src"
    override var descriptionsSelector: String = "ul.screen.thm-list>li>h4>a"
    override var descriptionsAttribute: String = "text"
    override var previousPageSelector: String = ""
    override var nextPageSelector: String = ""

    override fun elementInfosFromDocument(document: Document): List<ElementInfo> {
        val uris = document.parseListUri(
                selector = urisSelector,
                attribute = urisAttribute
        )
        val titles = document.parseListString(
                selector = titlesSelector,
                attribute = titlesAttribute
        )
        val thumbnails = document
                .parseElements(selector = thumbnailsSelector)
                .map { element ->
                    element
                            .attrOrText(thumbnailsAttribute)
                            .run {
                                if ("http://h.s.mangapark.me/img/blank.gif" == this)
                                    element.attrOrText("_src")
                                else
                                    this
                            }
                }
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
}