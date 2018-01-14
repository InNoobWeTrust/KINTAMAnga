package io.github.innoobwetrust.kintamanga.database.resolver.chapter

import android.database.Cursor
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_CHAPTER_INDEX
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_DESCRIPTION
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_ID
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_LAST_PAGE_READ
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_MANGA_ID
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_TITLE
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_UPDATE_TIME
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_URI
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_VIEWED

object ChapterGetResolver : DefaultGetResolver<ChapterDb>() {
    override fun mapFromCursor(cursor: Cursor): ChapterDb = ChapterDb().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        mangaId = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID))
        chapterUri = cursor.getString(cursor.getColumnIndex(COL_URI))
        chapterTitle = cursor.getString(cursor.getColumnIndex(COL_TITLE))
        chapterDescription = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION))
        chapterUpdateTime = cursor.getString(cursor.getColumnIndex(COL_UPDATE_TIME))
        chapterIndex = cursor.getInt(cursor.getColumnIndex(COL_CHAPTER_INDEX))
        chapterViewed = cursor.getInt(cursor.getColumnIndex(COL_VIEWED)) == 1
        chapterLastPageRead = cursor.getInt(cursor.getColumnIndex(COL_LAST_PAGE_READ))
    }
}