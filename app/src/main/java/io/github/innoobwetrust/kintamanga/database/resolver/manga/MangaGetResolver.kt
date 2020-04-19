package io.github.innoobwetrust.kintamanga.database.resolver.manga

import android.database.Cursor
import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_ALTERNATIVE_TITLE
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_ARTISTS_STRING
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_AUTHORS_STRING
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_DESCRIPTION
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_DOWNLOADED
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_FAVORITED
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_FLAGS
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_GENRES_STRING
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_ID
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_LAST_UPDATE
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_SOURCE_NAME
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_STATUS
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_THUMBNAIL_URI
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_TITLE
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_TRANSLATION_TEAMS_STRING
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_TYPES_STRING
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_URI
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_VIEWER
import io.github.innoobwetrust.kintamanga.database.table.MangaTable.COL_WARNING

object MangaGetResolver : DefaultGetResolver<MangaDb>() {
    override fun mapFromCursor(cursor: Cursor): MangaDb = MangaDb().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        flags = cursor.getInt(cursor.getColumnIndex(COL_FLAGS))
        mangaSourceName = cursor.getString(cursor.getColumnIndex(COL_SOURCE_NAME))
        mangaUri = cursor.getString(cursor.getColumnIndex(COL_URI))
        mangaTitle = cursor.getString(cursor.getColumnIndex(COL_TITLE))
        mangaAlternativeTitle = cursor.getString(cursor.getColumnIndex(COL_ALTERNATIVE_TITLE))
        mangaDescription = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION))
        mangaThumbnailUri = cursor.getString(cursor.getColumnIndex(COL_THUMBNAIL_URI))
        mangaArtistsString = cursor.getString(cursor.getColumnIndex(COL_ARTISTS_STRING))
        mangaAuthorsString = cursor.getString(cursor.getColumnIndex(COL_AUTHORS_STRING))
        mangaTranslationTeamsString = cursor.getString(cursor.getColumnIndex(COL_TRANSLATION_TEAMS_STRING))
        mangaStatus = cursor.getString(cursor.getColumnIndex(COL_STATUS))
        mangaTypesString = cursor.getString(cursor.getColumnIndex(COL_TYPES_STRING))
        mangaGenresString = cursor.getString(cursor.getColumnIndex(COL_GENRES_STRING))
        mangaWarning = cursor.getString(cursor.getColumnIndex(COL_WARNING))
        mangaFavorited = cursor.getInt(cursor.getColumnIndex(COL_FAVORITED)) == 1
        mangaDownloaded = cursor.getInt(cursor.getColumnIndex(COL_DOWNLOADED)) == 1
        mangaViewer = cursor.getInt(cursor.getColumnIndex(COL_VIEWER))
        mangaLastUpdate = cursor.getString(cursor.getColumnIndex(COL_LAST_UPDATE))
    }
}