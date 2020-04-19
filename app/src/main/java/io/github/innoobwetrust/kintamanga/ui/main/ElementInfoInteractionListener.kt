package io.github.innoobwetrust.kintamanga.ui.main

import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding

interface ElementInfoInteractionListener {
    fun onMangaCardClick(mangaBinding: MangaBinding)
    fun onRequestMoreElement() {}
}