package io.github.innoobwetrust.kintamanga.database.mapper

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import io.github.innoobwetrust.kintamanga.database.model.ChapterDb
import io.github.innoobwetrust.kintamanga.database.resolver.chapter.ChapterDeleteResolver
import io.github.innoobwetrust.kintamanga.database.resolver.chapter.ChapterGetResolver
import io.github.innoobwetrust.kintamanga.database.resolver.chapter.ChapterPutResolver

object ChapterTypeMapping: SQLiteTypeMapping<ChapterDb>(
        ChapterPutResolver,
        ChapterGetResolver,
        ChapterDeleteResolver
)