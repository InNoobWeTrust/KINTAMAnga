package io.github.innoobwetrust.kintamanga.database.mapper

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.resolver.manga.MangaDeleteResolver
import io.github.innoobwetrust.kintamanga.database.resolver.manga.MangaGetResolver
import io.github.innoobwetrust.kintamanga.database.resolver.manga.MangaPutResolver

object MangaTypeMapping : SQLiteTypeMapping<MangaDb>(
        MangaPutResolver,
        MangaGetResolver,
        MangaDeleteResolver
)