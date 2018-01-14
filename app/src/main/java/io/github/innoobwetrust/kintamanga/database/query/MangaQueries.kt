package io.github.innoobwetrust.kintamanga.database.query

import com.pushtorefresh.storio.sqlite.queries.Query
import io.github.innoobwetrust.kintamanga.database.DbProvider
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.resolver.manga.MangaDownloadedPutResolver
import io.github.innoobwetrust.kintamanga.database.table.MangaTable

interface MangaQueries : DbProvider {

    fun getMangas() = db.get()
            .listOfObjects(MangaDb::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .build())
            .prepare()

    fun getFavoriteMangas() = db.get()
            .listOfObjects(MangaDb::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_FAVORITED} = ?")
                    .whereArgs(1)
                    .orderBy(MangaTable.COL_TITLE)
                    .build())
            .prepare()

    fun getDownloadedMangas() = db.get()
            .listOfObjects(MangaDb::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_DOWNLOADED} = ?")
                    .whereArgs(1)
                    .orderBy(MangaTable.COL_TITLE)
                    .build())
            .prepare()

    fun getManga(uri: String, sourceName: String) = db.get()
            .`object`(MangaDb::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_URI} = ? AND ${MangaTable.COL_SOURCE_NAME} = ?")
                    .whereArgs(uri, sourceName)
                    .build())
            .prepare()

    fun getManga(id: Long) = db.get()
            .`object`(MangaDb::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_ID} = ?")
                    .whereArgs(id)
                    .build())
            .prepare()

    fun insertManga(mangaDb: MangaDb) = db.put().`object`(mangaDb).prepare()

    fun updateDownloadedField(mangaDb: MangaDb) = db.put()
            .`object`(mangaDb)
            .withPutResolver(MangaDownloadedPutResolver)
            .prepare()

    fun deleteManga(mangaDb: MangaDb) = db.delete().`object`(mangaDb).prepare()

}
