package io.github.innoobwetrust.kintamanga.database.resolver.chapter

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable

object ChapterPutResolver : DefaultPutResolver<ChapterDb>() {
    override fun mapToInsertQuery(obj: ChapterDb): InsertQuery = InsertQuery.builder()
            .table(ChapterTable.TABLE)
            .build()

    override fun mapToUpdateQuery(obj: ChapterDb): UpdateQuery = UpdateQuery.builder()
            .table(ChapterTable.TABLE)
            .where("${ChapterTable.COL_ID} = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: ChapterDb): ContentValues = ContentValues(10).apply {
        put(ChapterTable.COL_ID, obj.id)
        put(ChapterTable.COL_MANGA_ID, obj.mangaId)
        put(ChapterTable.COL_URI, obj.chapterUri)
        put(ChapterTable.COL_TITLE, obj.chapterTitle)
        put(ChapterTable.COL_DESCRIPTION, obj.chapterDescription)
        put(ChapterTable.COL_UPDATE_TIME, obj.chapterUpdateTime)
        put(ChapterTable.COL_CHAPTER_INDEX, obj.chapterIndex)
        put(ChapterTable.COL_VIEWED, obj.chapterViewed)
        put(ChapterTable.COL_LAST_PAGE_READ, obj.chapterLastPageRead)
    }
}