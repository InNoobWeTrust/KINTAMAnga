package io.github.innoobwetrust.kintamanga.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.InputStream

object ImageConverter {
    private val supportedImageTypes = listOf("image/gif", "image/png", "image/jpeg", "image/webp")
    val convertRequiredImageTypes = listOf("image/gif", "image/webp")

    // Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
    inline fun findImageMime(openStream: () -> InputStream): String? {
        try {
            openStream().buffered().use {
                val bytes = ByteArray(8)
                it.mark(bytes.size)
                val length = it.read(bytes, 0, bytes.size)
                it.reset()
                if (length == -1)
                    return null
                if (bytes[0] == 'G'.toByte()
                        && bytes[1] == 'I'.toByte()
                        && bytes[2] == 'F'.toByte()
                        && bytes[3] == '8'.toByte()) {
                    return "image/gif"
                } else if (bytes[0] == 0x89.toByte()
                        && bytes[1] == 0x50.toByte()
                        && bytes[2] == 0x4E.toByte()
                        && bytes[3] == 0x47.toByte()
                        && bytes[4] == 0x0D.toByte()
                        && bytes[5] == 0x0A.toByte()
                        && bytes[6] == 0x1A.toByte()
                        && bytes[7] == 0x0A.toByte()) {
                    return "image/png"
                } else if (bytes[0] == 0xFF.toByte()
                        && bytes[1] == 0xD8.toByte()
                        && bytes[2] == 0xFF.toByte()) {
                    return "image/jpeg"
                } else if (bytes[0] == 'W'.toByte()
                        && bytes[1] == 'E'.toByte()
                        && bytes[2] == 'B'.toByte()
                        && bytes[3] == 'P'.toByte()) {
                    return "image/webp"
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    @Throws(Exception::class)
    fun checkSupportedImage(target: File): Boolean {
        if (target.run { !isFile || !canRead() }) return false
        val mime = findImageMime { target.inputStream() } ?: return false
        return supportedImageTypes.contains(mime)
    }

    @Throws(Exception::class)
    fun convertToSupportedImage(source: File, target: File? = null): Boolean {
        if (source.run { !isFile || !canWrite() }) return false
        target?.let { if (!it.isFile || !it.canWrite()) return false }
        val mime = findImageMime { source.inputStream() } ?: return false
        if (!supportedImageTypes.contains(mime)) return false
        if (!convertRequiredImageTypes.contains(mime)) {
            return true
        } else {
            val tempImage = target ?:
                    File.createTempFile(
                            "img",
                            ".png",
                            source.parentFile.also { if (!it.canWrite()) return false }
                    )
            source.inputStream().buffered().use { inputStream ->
                tempImage.outputStream().buffered().use { outputStream ->
                    BitmapFactory
                            .decodeStream(inputStream)
                            ?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            ?: return false
                }
            }
            if (null == target) {
                tempImage.copyTo(target = source, overwrite = true)
                tempImage.delete()
            }
            System.gc()
            return true
        }
    }
}
