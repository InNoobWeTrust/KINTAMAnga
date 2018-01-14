package io.github.innoobwetrust.kintamanga.database.model

import androidx.databinding.BaseObservable
import io.github.innoobwetrust.kintamanga.model.Chapter
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding

open class ChapterDb : BaseObservable(), Chapter {
    open var id: Long? = null
    open var mangaId: Long? = null
    override var chapterUri: String = ""
    override var chapterTitle: String = ""
    override var chapterDescription: String = ""
    override var chapterUpdateTime: String = ""
    override var chapterIndex: Int = -1
    override var chapterViewed: Boolean = false
    override var chapterLastPageRead: Int = 0

    fun copyTo(chapterDb: ChapterDb) {
        chapterDb.let {
            it.id = this@ChapterDb.id
            it.mangaId = this@ChapterDb.mangaId
            it.chapterUri = this@ChapterDb.chapterUri
            it.chapterTitle = this@ChapterDb.chapterTitle
            it.chapterDescription = this@ChapterDb.chapterDescription
            it.chapterUpdateTime = this@ChapterDb.chapterUpdateTime
            it.chapterIndex = this@ChapterDb.chapterIndex
            it.chapterViewed = this@ChapterDb.chapterViewed
            it.chapterLastPageRead = this@ChapterDb.chapterLastPageRead
        }
    }

    fun copyFrom(chapterDb: ChapterDb) {
        chapterDb.let {
            this@ChapterDb.id = it.id
            this@ChapterDb.mangaId = it.mangaId
            this@ChapterDb.chapterUri = it.chapterUri
            this@ChapterDb.chapterTitle = it.chapterTitle
            this@ChapterDb.chapterDescription = it.chapterDescription
            this@ChapterDb.chapterUpdateTime = it.chapterUpdateTime
            this@ChapterDb.chapterIndex = it.chapterIndex
            this@ChapterDb.chapterViewed = it.chapterViewed
            this@ChapterDb.chapterLastPageRead = it.chapterLastPageRead
        }
    }

    fun asChapterBinding(): ChapterBinding = ChapterBinding().also { it.copyFrom(this@ChapterDb) }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ChapterDb) return false
        return other.chapterUri == this.chapterUri
    }

    override fun hashCode(): Int {
        return chapterUri.hashCode()
    }
}
