package io.github.innoobwetrust.kintamanga.database

import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite

interface DbProvider {
    val db: DefaultStorIOSQLite
}