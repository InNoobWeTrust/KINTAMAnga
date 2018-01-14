package io.github.innoobwetrust.kintamanga.database

import com.pushtorefresh.storio.sqlite.StorIOSQLite

inline fun StorIOSQLite.inTransaction(block: () -> Unit) {
    lowLevel().beginTransaction()
    try {
        block()
        lowLevel().setTransactionSuccessful()
    } finally {
        lowLevel().endTransaction()
    }
}

inline fun <T> StorIOSQLite.inTransactionReturn(block: () -> T): T {
    lowLevel().beginTransaction()
    return try {
        val result = block()
        lowLevel().setTransactionSuccessful()
        result
    } finally {
        lowLevel().endTransaction()
    }
}