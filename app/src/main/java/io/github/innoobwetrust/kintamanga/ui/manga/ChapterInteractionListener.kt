package io.github.innoobwetrust.kintamanga.ui.manga

import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding

interface ChapterInteractionListener {
    fun onLongClick(index: Int)
    fun onSelectionChanged(count: Int)
    fun onChapterClick(chapterBinding: ChapterBinding)
    fun onDownloadRequest(chapterBindings: List<ChapterBinding>, showDialog: Boolean)
    fun onDeleteRequest(chapterBindings: List<ChapterBinding>, showDialog: Boolean)
    fun onReadStatusToggled(chapterBindings: List<ChapterBinding>)
}