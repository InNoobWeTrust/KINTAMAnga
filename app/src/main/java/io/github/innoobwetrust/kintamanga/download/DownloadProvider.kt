package io.github.innoobwetrust.kintamanga.download

import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.ImageConverter
import io.github.innoobwetrust.kintamanga.util.Storage
import io.github.innoobwetrust.kintamanga.util.Storage.downloadDir
import io.github.innoobwetrust.kintamanga.util.extension.createDirectory
import io.github.innoobwetrust.kintamanga.util.extension.renameTo
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object DownloadProvider {
    @Throws(Exception::class)
    @Synchronized
    fun getMangaDir(
            sourceName: String,
            mangaTitle: String,
            createIfNotExist: Boolean = false
    ): File? {
        val mangaDir = File(
                File(downloadDir, Storage.buildValidFilename(sourceName)),
                Storage.buildValidFilename(mangaTitle)
        )
        if (!createIfNotExist && mangaDir.run { !isDirectory || !canRead() }) return null
        if (createIfNotExist && !mangaDir.exists() && !mangaDir.mkdirs()) return null
        return mangaDir
    }

    @Throws(Exception::class)
    @Synchronized
    fun getChapterDirs(
            sourceName: String,
            mangaTitle: String
    ): List<File>? {
        val mangaDir = getMangaDir(sourceName = sourceName, mangaTitle = mangaTitle) ?: return null
        return mangaDir.list()
                .map { File(mangaDir, it) }
                .filter { it.run { isDirectory && canRead() && listFiles().isNotEmpty() } }
    }

    @Throws(Exception::class)
    @Synchronized
    fun getChapterDir(
            sourceName: String,
            mangaTitle: String,
            chapterTitle: String
    ): File? {
        val mangaDir = getMangaDir(sourceName = sourceName, mangaTitle = mangaTitle) ?: return null
        val chapterDir = File(mangaDir, Storage.buildValidFilename(chapterTitle))
        if (chapterDir.run { !isDirectory || !canRead() }) return null
        if (chapterDir.listFiles().isEmpty()) {
            chapterDir.deleteRecursively()
            return null
        }
        return chapterDir
    }

    @Throws(Exception::class)
    @Synchronized
    fun createTempChapterDir(
            sourceName: String,
            mangaTitle: String,
            chapterTitle: String
    ): File? {
        val mangaDir = getMangaDir(
                sourceName = sourceName,
                mangaTitle = mangaTitle,
                createIfNotExist = true
        ) ?: return null
        return try {
            mangaDir.createDirectory("${Storage.buildValidFilename(chapterTitle)}_tmp")
        } catch (e: Exception) {
            null
        }
    }

    @Throws(Exception::class)
    @Synchronized
    fun getTempChapterDir(
            sourceName: String,
            mangaTitle: String,
            chapterTitle: String
    ): File? {
        val mangaDir = getMangaDir(
                sourceName = sourceName,
                mangaTitle = mangaTitle
        ) ?: return null
        val tmpDir = File(mangaDir, "${Storage.buildValidFilename(chapterTitle)}_tmp")
        return if (tmpDir.run { isDirectory && canWrite() }) tmpDir else null
    }

    @Throws(Exception::class)
    @Synchronized
    fun findExistingChapterDir(
            sourceName: String,
            mangaTitle: String,
            chapterTitle: String
    ): File? {
        val mangaDir = File(
                File(downloadDir, sourceName),
                Storage.buildValidFilename(mangaTitle)
        )
        val chapterDir = File(mangaDir, Storage.buildValidFilename(chapterTitle))
        if (chapterDir.run { !isDirectory || !canRead() }) return null
        return chapterDir
    }

    @Throws(Exception::class)
    @Synchronized
    private fun findChapterImages(chapterDir: File): List<Pair<Int, File>>? {
        if (chapterDir.run { !isDirectory || !canRead() }) return null
        // Remove invalid files
        return chapterDir.listFiles()
                .filter { null != "^[\\d]{3}$".toRegex().find(it.nameWithoutExtension) }
                .filter { it.run(ImageConverter::checkSupportedImage) }
                .sortedBy { it.nameWithoutExtension.toInt() }
                .distinctBy { it.nameWithoutExtension }
                .map { it.nameWithoutExtension.toInt() to it }
    }

    fun markDownloadedChapters(mangaBinding: MangaBinding): Boolean {
        val sortedChapters = mangaBinding.chapters
                .sortedBy { it.chapterIndex }
        val chapterDirNames = sortedChapters
                .map { Storage.buildValidFilename(it.chapterTitle) }
        try {
            val offlineDirs = getChapterDirs(
                    sourceName = mangaBinding.mangaSourceName,
                    mangaTitle = mangaBinding.mangaTitle
            )?.filter { dir -> chapterDirNames.any { it == dir.name } } ?: return false
            // mark as available offline for non-expired chapters and un-mark invalid downloaded chapter
            val downloadedFound = AtomicBoolean(false)
            sortedChapters.forEachIndexed { index, chapterBinding ->
                val offlineChapterDir = offlineDirs.find { it.name == chapterDirNames[index] }
                if (null != offlineChapterDir) {
                    chapterBinding.chapterDownloadStatus = DownloadStatus.DOWNLOADED
                    downloadedFound.compareAndSet(false, true)
                } else if (DownloadStatus.DOWNLOADED == chapterBinding.chapterDownloadStatus) {
                    chapterBinding.chapterDownloadStatus = DownloadStatus.NOT_DOWNLOADED
                }
            }
            if (downloadedFound.get()) mangaBinding.mangaDownloaded = true
            return downloadedFound.get()
        } catch (e: Exception) {
            return false
        }
    }

    fun injectDownloadedChapterImages(
            mangaBinding: MangaBinding,
            chapterIndex: Int
    ) {
        if (!mangaBinding.chapters.indices.contains(chapterIndex)) return
        try {
            val chapterBinding = mangaBinding.chapters[chapterIndex]
            val chapterDir = getChapterDir(
                    sourceName = mangaBinding.mangaSourceName,
                    mangaTitle = mangaBinding.mangaTitle,
                    chapterTitle = chapterBinding.chapterTitle
            )
            // Alternate image Uris for non-expired chapters
            if (null == chapterDir) {
                chapterBinding.chapterDownloadStatus = DownloadStatus.NOT_DOWNLOADED
                return
            }
            val images = findChapterImages(chapterDir = chapterDir)
                    ?: throw Exception("Error occur when indexing offline chapter's images")
            val tempMutableList = mutableListOf<String>()
            var maxNum = images.maxBy { it.first }?.first ?: 1
            if (maxNum < 1) maxNum = 1
            (1..maxNum).asSequence().map { index -> images.find { it.first == index } }.forEach {
                if (null != it)
                    tempMutableList.add("file://" + it.second.absolutePath)
                else
                    tempMutableList.add("")
            }
            chapterBinding.chapterPages = tempMutableList.mapIndexed { index, s ->
                Page(
                        chapterIndex = chapterBinding.chapterIndex,
                        pageIndex = index,
                        imageFileUri = s
                )
            }
        } catch (e: Exception) {
        }
    }

    @Synchronized
    fun onUpgradeSourceName(oldSourceName: String, newSourceName: String): Boolean {
        return try {
            File(downloadDir, Storage.buildValidFilename(oldSourceName))
                    .renameTo(Storage.buildValidFilename(newSourceName))
            true
        } catch (e: Exception) {
            false
        }
    }
}