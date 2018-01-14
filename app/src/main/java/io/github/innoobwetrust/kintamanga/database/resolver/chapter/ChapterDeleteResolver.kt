package io.github.innoobwetrust.kintamanga.database.resolver.chapter

import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.COL_ID
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable.TABLE

object ChapterDeleteResolver : DefaultDeleteResolver<ChapterDb>() {
    override fun mapToDeleteQuery(obj: ChapterDb) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}