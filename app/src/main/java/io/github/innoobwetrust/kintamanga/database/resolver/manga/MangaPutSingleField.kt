package io.github.innoobwetrust.kintamanga.database.resolver.manga

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.MangaTable

interface MangaPutSingleField {
    fun mapToUpdateQuery(mangaDb: MangaDb) = UpdateQuery.builder()
            .table(MangaTable.TABLE)
            .where("${MangaTable.COL_ID} = ?")
            .whereArgs(mangaDb.id)
            .build()

    fun mapToContentValues(mangaDb: MangaDb): ContentValues
}