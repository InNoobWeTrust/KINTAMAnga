package io.github.innoobwetrust.kintamanga.util.extension

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL

fun Element.attrOrText(css: String): String {
    return if (css.isNotEmpty() && "text" != css) attr(css) else if ("text" == css) text() else ""
}

fun Element.parseElements(
        selector: String
): Elements = if (selector.isBlank()) Elements(this) else select(selector)

fun Element.parseString(
        selector: String,
        attribute: String
): String {
    val targetElement =
            if (selector.isNotEmpty())
                select(selector).first()
            else
                this
    return targetElement?.attrOrText(attribute) ?: ""
}

fun Element.parseListString(
        selector: String,
        attribute: String
): List<String> = parseElements(selector = selector).map {
    it.parseString(selector = "", attribute = attribute)
}

fun Element.parseUri(
        selector: String,
        attribute: String
): String {
    val targetElement =
            if (selector.isNotEmpty())
                select(selector).first()
            else
                this
    val result = if (attribute.isBlank() || "text" == attribute)
        targetElement?.attrOrText(attribute)
    else
        targetElement?.absUrl(attribute)
    return try {
        val baseUrl = URL(this.baseUri())
        result?.uriString(baseUrl) ?: ""
    } catch (e: Exception) {
        result?.uriString ?: ""
    }
}

fun Element.parseListUri(
        selector: String,
        attribute: String
): List<String> = parseElements(selector = selector).map {
    it.parseUri(selector = "", attribute = attribute)
}