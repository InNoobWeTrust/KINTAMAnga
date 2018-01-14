package io.github.innoobwetrust.kintamanga.database.query

import com.pushtorefresh.storio.sqlite.queries.Query
import io.github.innoobwetrust.kintamanga.database.DbProvider
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable

interface ChapterQueries : DbProvider {

    fun getChapters(mangaDb: MangaDb) = db.get()
            .listOfObjects(ChapterDb::class.java)
            .withQuery(Query.builder()
                    .table(ChapterTable.TABLE)
                    .where("${ChapterTable.COL_MANGA_ID} = ?")
                    .whereArgs(mangaDb.id)
                    .build())
            .prepare()

    fun getChapter(id: Long) = db.get()
            .`object`(ChapterDb::class.java)
            .withQuery(Query.builder()
                    .table(ChapterTable.TABLE)
                    .where("${ChapterTable.COL_ID} = ?")
                    .whereArgs(id)
                    .build())
            .prepare()


    fun insertChapters(chapterDbs: List<ChapterDb>) = db.put().objects(chapterDbs).prepare()

    fun deleteChapters(chapterDbs: List<ChapterDb>) = db.delete().objects(chapterDbs).prepare()
}
