package io.github.innoobwetrust.kintamanga.model

import java.io.Serializable

interface Chapter : Serializable {
    var chapterUri: String
    var chapterTitle: String
    var chapterDescription: String
    var chapterUpdateTime: String
    var chapterIndex: Int
    var chapterViewed: Boolean
    var chapterLastPageRead: Int

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}