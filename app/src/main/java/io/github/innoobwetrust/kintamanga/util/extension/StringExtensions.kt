package io.github.innoobwetrust.kintamanga.util.extension

import timber.log.Timber
import java.net.URI
import java.net.URL

val String.uriString: String
    get() = if (isBlank()) "" else try {
        URL(this).run {
            // If it's already encoded, skip or it will generate wrong result
            if (query?.contains("%") == true)
                toString()
            else
                URI(protocol, userInfo, host, port, path, query, ref)
                        .toASCIIString()
        }
    } catch (e: Exception) {
        Timber.e("$this: $e")
        ""
    }

fun String.uriString(context: URL): String = if (isBlank()) "" else try {
    URL(context, this).run {
        // If it's already encoded, skip or it will generate wrong result
        if (query?.contains("%") == true)
            toString()
        else
            URI(protocol, userInfo, host, port, path, query, ref)
                    .toASCIIString()
    }
} catch (e: Exception) {
    Timber.e("$this: $e")
    ""
}

// Steal from Tachiyomi app (https://github.com/inorichi/tachiyomi)
/**
 * Replaces the given string to have at most [count] characters using [replacement] at its end.
 * If [replacement] is longer than [count] an exception will be thrown when `length > count`.
 */
fun String.chop(count: Int, replacement: String = "..."): String {
    return if (length > count)
        take(count - replacement.length) + replacement
    else
        this

}
