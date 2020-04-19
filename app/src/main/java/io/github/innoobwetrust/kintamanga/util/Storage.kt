package io.github.innoobwetrust.kintamanga.util

import android.os.Environment
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.util.extension.createDirectory
import java.io.File

object Storage : KodeinGlobalAware {
    private val htmlCache: Pair<String, Long> = "okHttp" to 10 * 1024 * 1024
    private val coverCache: Pair<String, Long> = "cover" to 10 * 1024 * 1024
    private val chapterCache: Pair<String, Long> = "chapter" to 50 * 1024 * 1024

    private val htmlExternalCache: Pair<String, Long> = "okHttp" to 15 * 1024 * 1024
    val glideCache: Pair<String, Long> = "glide" to 10 * 1024 * 1024
    private val coverExternalCache: Pair<String, Long> = "cover" to 50 * 1024 * 1024
    private val chapterExternalCache: Pair<String, Long> = "chapter" to 100 * 1024 * 1024

//    const val PERMISSION_REQUEST_CODE: Int = 9999

    private val externalStorageAvailable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    private val externalCacheAccessible: Boolean
        get() = externalStorageAvailable
                && instance<File>("externalCache").run {
            this.exists() || this.mkdirs()
        }

    private val externalFileAccessible: Boolean
        get() = externalStorageAvailable
                && instance<File>("externalFiles").run {
            this.exists() || this.mkdirs()
        }

//    val cacheDir: File
//        get() = if (externalCacheAccessible)
//            instance("externalCache")
//        else instance("cache")

    val htmlCacheDir: File
        get() = if (externalCacheAccessible)
            File(instance<File>("externalCache"), htmlExternalCache.first)
        else File(instance<File>("cache"), htmlCache.first)

    val htmlCacheSize: Long
        get() = if (externalCacheAccessible)
            htmlExternalCache.second
        else htmlCache.second

    val coverCacheDir: File
        get() = if (externalCacheAccessible)
            File(instance<File>("externalCache"), coverExternalCache.first)
        else File(instance<File>("cache"), coverCache.first)

    val coverCacheSize: Long
        get() = if (externalCacheAccessible)
            coverExternalCache.second
        else coverCache.second

    val chapterCacheDir: File
        get() = if (externalCacheAccessible)
            File(instance<File>("externalCache"), chapterExternalCache.first)
        else File(instance<File>("cache"), chapterCache.first)

    val chapterCacheSize: Long
        get() = if (externalCacheAccessible)
            chapterExternalCache.second
        else chapterCache.second

    private val privateFilesDir: File
        get() = if (externalFileAccessible)
            instance("externalFiles")
        else
            instance("files")

    val downloadDir: File
        get() = privateFilesDir.createDirectory("download")

//    @JvmStatic
//    fun excludeDirFromMediaScanner(dir: File): Boolean {
//        if (dir.isDirectory) {
//            try {
//                File(dir, ".nomedia").createNewFile()
//                return true
//            } catch (e: Exception) {
//                return false
//            }
//        }
//        return false
//    }

    // Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_". This method doesn't allow hidden files (starting
     * with a dot), but you can manually add it later.
     */
    fun buildValidFilename(origName: String): String {
        val name = origName.trim('.', ' ')
        if (name.isEmpty()) {
            return "(invalid)"
        }
        val sb = StringBuilder(name.length)
        name.forEach { c ->
            if (isValidFatFilenameChar(c)) {
                sb.append(c)
            } else {
                sb.append('_')
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit minus 15 reserved characters.
        return sb.toString().take(240)
    }

    // Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
    /**
     * Returns true if the given character is a valid filename character, false otherwise.
     */
    private fun isValidFatFilenameChar(c: Char): Boolean {
        if (0x00.toChar() <= c && c <= 0x1f.toChar()) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7f.toChar() -> false
            else -> true
        }
    }

//    fun getDirectorySize(dir: File): Long = dir.walkBottomUp().fold(
//            0L,
//            { acc, file ->
//                if (file.isFile)
//                    acc + file.length()
//                else
//                    acc
//            }
//    )
//
//    // Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
//    fun requestPermissions(activity: AppCompatActivity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (
//            ContextCompat.checkSelfPermission(
//                    activity,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                ActivityCompat.requestPermissions(
//                        activity,
//                        arrayOf(
//                                Manifest.permission.READ_EXTERNAL_STORAGE,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        ),
//                        PERMISSION_REQUEST_CODE
//                )
//            }
//        }
//    }
//
//    fun getExternalAppDirs(context: Context): Collection<File> {
//        val directories = mutableSetOf<File>()
//        directories += ContextCompat.getExternalFilesDirs(context, null)
//                .filterNotNull()
//                .mapNotNull {
//                    val state = EnvironmentCompat.getStorageState(it)
//                    if (state == Environment.MEDIA_MOUNTED ||
//                            state == Environment.MEDIA_MOUNTED_READ_ONLY) {
//                        it
//                    } else {
//                        null
//                    }
//                }
//        return directories
//    }
//
//    // Borrow from Tachiyomi app (https://github.com/inorichi/tachiyomi)
//    fun getExternalStorages(context: Context): Collection<File> {
//        val directories = mutableSetOf<File>()
//        directories += ContextCompat.getExternalFilesDirs(context, null)
//                .filterNotNull()
//                .mapNotNull {
//                    val file = File(it.absolutePath.substringBefore("/Android/"))
//                    val state = EnvironmentCompat.getStorageState(file)
//                    if (state == Environment.MEDIA_MOUNTED ||
//                            state == Environment.MEDIA_MOUNTED_READ_ONLY) {
//                        file
//                    } else {
//                        null
//                    }
//                }
//        if (Build.VERSION.SDK_INT < 21) {
//            val extStorages = System.getenv("SECONDARY_STORAGE")
//            if (extStorages != null) {
//                directories += extStorages.split(":").map(::File)
//            }
//        }
//        return directories
//    }
//
//    @Throws(Exception::class)
//    fun checkOlderThanDays(fileOrDir: File, days: Int): Boolean {
//        if (!fileOrDir.exists()) throw Exception("File or folder at path '$fileOrDir' does not exist")
//        val lastModified = fileOrDir.lastModified()
//        if (0 < lastModified) {
//            val lastModifiedDate = Date(lastModified)
//            val now = Date(System.currentTimeMillis())
//            val diffDays = (now.time - lastModifiedDate.time) / (24 * 3600 * 1000)
//            if (days < diffDays) return true
//            return false
//        }
//        throw Exception("Can't read last modified time for file or folder at path $fileOrDir")
//    }
//
//    @Throws(Exception::class)
//    fun checkEmptyFolder(dir: File?): Boolean {
//        // Check either directory not exists or not a diretory
//        if (null == dir || !dir.isDirectory) return false
//        if (dir.list().isEmpty()) return true
//        throw Exception("Can't check empty folder at path $dir, unknown error")
//    }
//
//    @Throws(Exception::class)
//    fun deleteOldFilesAndDirectories(fileOrDir: File?, days: Int): Boolean {
//        if (null == fileOrDir) return false
//        if (fileOrDir.isDirectory) {
//            val children = fileOrDir.list()
//            children.asSequence()
//                    .filter { checkOlderThanDays(fileOrDir = File(fileOrDir, it), days = days) }
//                    .map {
//                        deleteOldFilesAndDirectories(fileOrDir = File(fileOrDir, it), days = days)
//                    }.filterNot { it }
//                    .forEach { return false }
//            // Old files inside subdirectories deleted, check any old empty folder to delete
//            fileOrDir.list().asSequence()
//                    .map { File(fileOrDir, it) }
//                    .filter {
//                        checkEmptyFolder(it) && checkOlderThanDays(fileOrDir = it, days = days)
//                    }.map { it.delete() }
//                    .filterNot { it }
//                    .forEach { return false }
//            return true
//        }
//        // If we're about to check a file, not directory
//        if (fileOrDir.isFile && checkOlderThanDays(fileOrDir = fileOrDir, days = days))
//            return fileOrDir.delete()
//        throw Exception(
//                "Unknown exception when checking old files or directories at path $fileOrDir"
//        )
//    }
}