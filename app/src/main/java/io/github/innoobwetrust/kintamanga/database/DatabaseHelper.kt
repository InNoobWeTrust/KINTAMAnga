package io.github.innoobwetrust.kintamanga.database

import android.content.Context
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite
import io.github.innoobwetrust.kintamanga.database.mapper.ChapterTypeMapping
import io.github.innoobwetrust.kintamanga.database.mapper.MangaTypeMapping
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.query.ChapterQueries
import io.github.innoobwetrust.kintamanga.database.query.MangaQueries

class DatabaseHelper(context: Context)
    : MangaQueries, ChapterQueries {

    override val db = DefaultStorIOSQLite.builder()
            .sqliteOpenHelper(DbOpenHelper(context))
            .addTypeMapping(MangaDb::class.java, MangaTypeMapping)
            .addTypeMapping(ChapterDb::class.java, ChapterTypeMapping)
            .build()

}
