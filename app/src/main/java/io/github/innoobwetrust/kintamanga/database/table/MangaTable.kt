package io.github.innoobwetrust.kintamanga.database.table

object MangaTable {
    const val TABLE = "manga"
    const val COL_ID = "_id"
    const val COL_FLAGS = "flag"
    const val COL_SOURCE_NAME = "source_name"
    const val COL_URI = "uri"
    const val COL_TITLE = "title"
    const val COL_ALTERNATIVE_TITLE = "alternative_title"
    const val COL_DESCRIPTION = "description"
    const val COL_THUMBNAIL_URI = "thumbnail_uri"
    const val COL_ARTISTS_STRING = "artists_string"
    const val COL_AUTHORS_STRING = "authors_string"
    const val COL_TRANSLATION_TEAMS_STRING = "translation_teams_string"
    const val COL_STATUS = "status"
    const val COL_TYPES_STRING = "types_string"
    const val COL_GENRES_STRING = "genres_string"
    const val COL_WARNING = "warning"
    const val COL_FAVORITED = "favorite"
    const val COL_DOWNLOADED = "downloaded"
    const val COL_VIEWER = "viewer"
    const val COL_LAST_UPDATE = "last_update"

    val createTableQuery: String
        get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER PRIMARY KEY,
            $COL_FLAGS INTEGER NOT NULL,
            $COL_SOURCE_NAME TEXT NOT NULL,
            $COL_URI TEXT NOT NULL,
            $COL_TITLE TEXT NOT NULL,
            $COL_ALTERNATIVE_TITLE TEXT,
            $COL_DESCRIPTION TEXT,
            $COL_THUMBNAIL_URI TEXT,
            $COL_ARTISTS_STRING TEXT,
            $COL_AUTHORS_STRING TEXT,
            $COL_TRANSLATION_TEAMS_STRING TEXT,
            $COL_STATUS TEXT,
            $COL_TYPES_STRING TEXT,
            $COL_GENRES_STRING TEXT,
            $COL_WARNING TEXT,
            $COL_FAVORITED BOOLEAN NOT NULL,
            $COL_DOWNLOADED BOOLEAN NOT NULL,
            $COL_VIEWER INTEGER NOT NULL,
            $COL_LAST_UPDATE TEXT
            )"""

    val createSourceNameIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_SOURCE_NAME}_index ON $TABLE($COL_SOURCE_NAME)"

    val createUriIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_URI}_index ON $TABLE($COL_URI)"

    val createFavoriteIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_FAVORITED}_index ON $TABLE($COL_FAVORITED)"

    val createDownloadIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_DOWNLOADED}_index ON $TABLE($COL_DOWNLOADED)"
}