package io.github.innoobwetrust.kintamanga.database.resolver.manga

import android.content.ContentValues
import com.pushtorefresh.storio.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.sqlite.queries.InsertQuery
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.database.table.MangaTable

object MangaPutResolver : DefaultPutResolver<MangaDb>() {

    override fun mapToInsertQuery(obj: MangaDb) = InsertQuery.builder()
            .table(MangaTable.TABLE)
            .build()

    override fun mapToUpdateQuery(obj: MangaDb) = UpdateQuery.builder()
            .table(MangaTable.TABLE)
            .where("${MangaTable.COL_ID} = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: MangaDb) = ContentValues(20).apply {
        put(MangaTable.COL_ID, obj.id)
        put(MangaTable.COL_FLAGS, obj.flags)
        put(MangaTable.COL_SOURCE_NAME, obj.mangaSourceName)
        put(MangaTable.COL_URI, obj.mangaUri)
        put(MangaTable.COL_TITLE, obj.mangaTitle)
        put(MangaTable.COL_ALTERNATIVE_TITLE, obj.mangaAlternativeTitle)
        put(MangaTable.COL_DESCRIPTION, obj.mangaDescription)
        put(MangaTable.COL_THUMBNAIL_URI, obj.mangaThumbnailUri)
        put(MangaTable.COL_ARTISTS_STRING, obj.mangaArtistsString)
        put(MangaTable.COL_AUTHORS_STRING, obj.mangaAuthorsString)
        put(MangaTable.COL_TRANSLATION_TEAMS_STRING, obj.mangaTranslationTeamsString)
        put(MangaTable.COL_STATUS, obj.mangaStatus)
        put(MangaTable.COL_TYPES_STRING, obj.mangaTypesString)
        put(MangaTable.COL_GENRES_STRING, obj.mangaGenresString)
        put(MangaTable.COL_WARNING, obj.mangaWarning)
        put(MangaTable.COL_FAVORITED, obj.mangaFavorited)
        put(MangaTable.COL_DOWNLOADED, obj.mangaDownloaded)
        put(MangaTable.COL_VIEWER, obj.mangaViewer)
        put(MangaTable.COL_LAST_UPDATE, obj.mangaLastUpdate)
    }
}