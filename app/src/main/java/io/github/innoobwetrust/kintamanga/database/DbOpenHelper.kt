package io.github.innoobwetrust.kintamanga.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.github.innoobwetrust.kintamanga.database.table.ChapterTable
import io.github.innoobwetrust.kintamanga.database.table.MangaTable
import io.github.innoobwetrust.kintamanga.download.DownloadProvider
import io.github.innoobwetrust.kintamanga.source.instance.vi.hocvientruyentranh.HocVienTruyenTranhSource

class DbOpenHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        /**
         * Name of the database file.
         */
        const val DATABASE_NAME = "kintamanga.db"

        /**
         * Version of the database.
         */
        const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(MangaTable.createTableQuery)
        execSQL(ChapterTable.createTableQuery)

        // DB indexes
        execSQL(MangaTable.createSourceNameIndexQuery)
        execSQL(MangaTable.createUriIndexQuery)
        execSQL(MangaTable.createFavoriteIndexQuery)
        execSQL(MangaTable.createDownloadIndexQuery)
        execSQL(ChapterTable.createMangaIdIndexQuery)
        execSQL(ChapterTable.createUriIndexQuery)
        execSQL(ChapterTable.createViewedIndexQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            1 -> with(db) {
                execSQL("ALTER TABLE ${MangaTable.TABLE} RENAME TO old_manga")
                execSQL("ALTER TABLE ${ChapterTable.TABLE} RENAME TO old_chapter")
                execSQL(MangaTable.createTableQuery)
                execSQL(ChapterTable.createTableQuery)
                execSQL("""INSERT INTO ${MangaTable.TABLE}(
                    ${MangaTable.COL_ID},
                    ${MangaTable.COL_FLAGS},
                    ${MangaTable.COL_SOURCE_NAME},
                    ${MangaTable.COL_URI},
                    ${MangaTable.COL_TITLE},
                    ${MangaTable.COL_ALTERNATIVE_TITLE},
                    ${MangaTable.COL_DESCRIPTION},
                    ${MangaTable.COL_THUMBNAIL_URI},
                    ${MangaTable.COL_ARTISTS_STRING},
                    ${MangaTable.COL_AUTHORS_STRING},
                    ${MangaTable.COL_TRANSLATION_TEAMS_STRING},
                    ${MangaTable.COL_STATUS},
                    ${MangaTable.COL_TYPES_STRING},
                    ${MangaTable.COL_GENRES_STRING},
                    ${MangaTable.COL_WARNING},
                    ${MangaTable.COL_FAVORITED},
                    ${MangaTable.COL_DOWNLOADED},
                    ${MangaTable.COL_VIEWER},
                    ${MangaTable.COL_LAST_UPDATE}
                    ) SELECT
                    ${MangaTable.COL_ID},
                    ${MangaTable.COL_FLAGS},
                    ${MangaTable.COL_SOURCE_NAME},
                    ${MangaTable.COL_URI},
                    ${MangaTable.COL_TITLE},
                    ${MangaTable.COL_ALTERNATIVE_TITLE},
                    ${MangaTable.COL_DESCRIPTION},
                    ${MangaTable.COL_THUMBNAIL_URI},
                    ${MangaTable.COL_ARTISTS_STRING},
                    ${MangaTable.COL_AUTHORS_STRING},
                    ${MangaTable.COL_TRANSLATION_TEAMS_STRING},
                    ${MangaTable.COL_STATUS},
                    ${MangaTable.COL_TYPES_STRING},
                    ${MangaTable.COL_GENRES_STRING},
                    ${MangaTable.COL_WARNING},
                    ${MangaTable.COL_FAVORITED},
                    ${MangaTable.COL_DOWNLOADED},
                    ${MangaTable.COL_VIEWER},
                    ${MangaTable.COL_LAST_UPDATE}
                    FROM old_manga""")
                execSQL("""INSERT INTO ${ChapterTable.TABLE}(
                    ${ChapterTable.COL_ID},
                    ${ChapterTable.COL_MANGA_ID},
                    ${ChapterTable.COL_URI},
                    ${ChapterTable.COL_TITLE},
                    ${ChapterTable.COL_DESCRIPTION},
                    ${ChapterTable.COL_UPDATE_TIME},
                    ${ChapterTable.COL_CHAPTER_INDEX},
                    ${ChapterTable.COL_VIEWED},
                    ${ChapterTable.COL_LAST_PAGE_READ}
                    ) SELECT
                    ${ChapterTable.COL_ID},
                    ${ChapterTable.COL_MANGA_ID},
                    ${ChapterTable.COL_URI},
                    ${ChapterTable.COL_TITLE},
                    ${ChapterTable.COL_DESCRIPTION},
                    ${ChapterTable.COL_UPDATE_TIME},
                    ${ChapterTable.COL_CHAPTER_INDEX},
                    ${ChapterTable.COL_VIEWED},
                    ${ChapterTable.COL_LAST_PAGE_READ}
                    FROM old_chapter""")
                execSQL("DROP TABLE old_manga")
                execSQL("DROP TABLE old_chapter")
                DownloadProvider.onUpgradeSourceName("AcademyVN", HocVienTruyenTranhSource.sourceName)
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }
}