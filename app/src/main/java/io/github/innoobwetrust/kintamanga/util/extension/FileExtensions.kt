package io.github.innoobwetrust.kintamanga.util.extension

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.github.innoobwetrust.kintamanga.BuildConfig
import java.io.File

// Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", this)
    else Uri.fromFile(this)
}

@Throws(Exception::class)
fun File.createFile(fileName: String): File = File(this, fileName).let {
    if (it.isFile || it.createNewFile()) it else throw Exception("Can not create file")
}

@Throws(Exception::class)
fun File.createDirectory(directoryName: String): File = File(this, directoryName).let {
    if (it.isDirectory || it.mkdirs()) it else throw Exception("Can not create directory")
}

@Throws(Exception::class)
fun File.renameTo(newName: String): Boolean =
        renameTo(File(parentFile ?: throw Exception("File not in a directory"), newName))

@Throws(Exception::class)
fun File.findFile(fileName: String): File? = listFiles()?.find { fileName == it.name }

@Throws(Exception::class)
fun File.findFileIgnoreExtension(fileNameWithoutExtension: String) =
        listFiles()?.find { fileNameWithoutExtension == it.nameWithoutExtension }
