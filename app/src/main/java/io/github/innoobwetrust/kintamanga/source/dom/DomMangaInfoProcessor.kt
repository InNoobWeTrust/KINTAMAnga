package io.github.innoobwetrust.kintamanga.source.dom

import io.github.innoobwetrust.kintamanga.source.dom.parser.DomMangaParser
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument

interface DomMangaInfoProcessor : MangaInfoProcessor, DomMangaParser {
    @Throws(Exception::class)
    override fun fetchManga(uri: String): MangaBinding {
        val document = fetchData(
                uri = uri,
                headers = headers(),
                cacheControl = cacheControl()
        ).also { if(!it.isSuccessful) throw Exception("Failed to fetch manga") }.asJsoupDocument()
        return mangaFromDocument(document = document) ?: throw Exception("Failed to parse manga")
    }
}