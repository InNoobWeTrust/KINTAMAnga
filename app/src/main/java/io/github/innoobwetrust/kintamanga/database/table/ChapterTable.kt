package io.github.innoobwetrust.kintamanga.database.table

object ChapterTable {
    const val TABLE = "chapter"
    const val COL_ID = "_id"
    const val COL_MANGA_ID = "manga_id"
    const val COL_URI = "uri"
    const val COL_TITLE = "title"
    const val COL_DESCRIPTION = "description"
    const val COL_UPDATE_TIME = "update_time"
    const val COL_CHAPTER_INDEX = "number"
    const val COL_VIEWED = "viewed"
    const val COL_LAST_PAGE_READ = "last_page_read"

    val createTableQuery: String
        get() = """CREATE TABLE $TABLE(
            $COL_ID INTEGER PRIMARY KEY,
            $COL_MANGA_ID INTEGER NOT NULL,
            $COL_URI TEXT NOT NULL,
            $COL_TITLE TEXT NOT NULL,
            $COL_DESCRIPTION TEXT NOT NULL,
            $COL_UPDATE_TIME TEXT NOT NULL,
            $COL_CHAPTER_INDEX INTEGER NOT NULL,
            $COL_VIEWED BOOLEAN DEFAULT FALSE,
            $COL_LAST_PAGE_READ INTEGER DEFAULT 0,
            FOREIGN KEY($COL_MANGA_ID) REFERENCES ${MangaTable.TABLE} (${MangaTable.COL_ID})
            ON DELETE CASCADE
            )"""

    val createMangaIdIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_MANGA_ID}_index ON $TABLE($COL_MANGA_ID)"

    val createUriIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_URI}_index ON $TABLE($COL_URI)"

    val createViewedIndexQuery: String
        get() = "CREATE INDEX ${TABLE}_${COL_VIEWED}_index ON $TABLE($COL_VIEWED)"
}