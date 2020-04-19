package io.github.innoobwetrust.kintamanga.database.resolver.manga

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.StorIOSQLite
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio.sqlite.operations.put.PutResult
import io.github.innoobwetrust.kintamanga.database.inTransactionReturn
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.MangaTable

object MangaLastUpdatePutResolver : PutResolver<MangaDb>(), MangaPutSingleField {
    override fun performPut(db: StorIOSQLite, mangaDb: MangaDb): PutResult =
            db.inTransactionReturn {
                val updateQuery = mapToUpdateQuery(mangaDb)
                val contentValues = mapToContentValues(mangaDb)

                val numberOfRowsUpdated =
                        db.lowLevel().update(updateQuery, contentValues)
                PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
            }

    override fun mapToContentValues(mangaDb: MangaDb) = ContentValues(1).apply {
        put(MangaTable.COL_LAST_UPDATE, mangaDb.mangaLastUpdate)
    }
}