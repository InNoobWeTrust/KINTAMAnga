package io.github.innoobwetrust.kintamanga.database.resolver.manga

import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_ID
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.TABLE

object MangaDeleteResolver : DefaultDeleteResolver<MangaDb>() {
    override fun mapToDeleteQuery(obj: MangaDb) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}