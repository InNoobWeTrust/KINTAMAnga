package io.github.innoobwetrust.kintamanga

import java.io.InputStream

object AssetReader {
    @Throws(Exception::class)
    fun readAssetAsString(filePath: String): String {
        val inputStream: InputStream =
                this::class.java.classLoader!!.getResourceAsStream(filePath) ?: return ""
        return inputStream.use { stream ->
            stream.reader(Charsets.UTF_8).use { reader -> reader.readText() }
        }
    }
}
