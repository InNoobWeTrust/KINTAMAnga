package io.github.innoobwetrust.kintamanga.source.processor

import io.github.innoobwetrust.kintamanga.model.Manga
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.Response
import java.text.DateFormat
import java.util.*

interface MangaInfoProcessor : BaseInfoProcessor {
    @Throws(Exception::class)
    fun fetchData(
            manga: Manga,
            headers: Headers = headers(),
            cacheControl: CacheControl = cacheControl()
    ): Response = fetchData(
            uri = manga.mangaUri,
            headers = headers,
            cacheControl = cacheControl
    )

    @Throws(Exception::class)
    fun fetchManga(uri: String): MangaBinding

    @Throws(Exception::class)
    fun fetchFullInfo(mangaBinding: MangaBinding): Boolean {
        try {
            mangaBinding.apply {
                fetchManga(mangaUri).let {
                    mangaSourceName = it.mangaSourceName
                    mangaUri = it.mangaUri
                    // Empty title means something is wrong, may be a 404 or manga is removed
                    mangaTitle = it.mangaTitle
                    mangaAlternativeTitle = it.mangaAlternativeTitle
                    mangaDescription = it.mangaDescription
                    mangaThumbnailUri = it.mangaThumbnailUri
                    mangaArtistsString = it.mangaArtistsString
                    mangaAuthorsString = it.mangaAuthorsString
                    mangaTranslationTeamsString = it.mangaTranslationTeamsString
                    mangaStatus = it.mangaStatus
                    mangaTypesString = it.mangaTypesString
                    mangaGenresString = it.mangaGenresString
                    mangaWarning = it.mangaWarning
                    chapters = it.chapters.onEach { netChap ->
                        chapters.find { chapter -> chapter.chapterIndex == netChap.chapterIndex }?.let { chapterBinding ->
                            netChap.id = chapterBinding.id
                            netChap.mangaId = chapterBinding.mangaId
                            // Usually, a change in title means update (RAW, EN to translated)
                            if(netChap.chapterTitle == chapterBinding.chapterTitle) {
                                netChap.chapterViewed = chapterBinding.chapterViewed
                                netChap.chapterLastPageRead = chapterBinding.chapterLastPageRead
                            }
                        }
                    }
                    mangaLastUpdate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
