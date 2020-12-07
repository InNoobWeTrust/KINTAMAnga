package io.github.innoobwetrust.kintamanga.ui.manga

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.MaterialCab
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.factory
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.Gson
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.ActivityMangaInfoBinding
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.service.DownloadService
import io.github.innoobwetrust.kintamanga.source.SourceManager
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.ui.reader.ReaderActivity
import io.github.innoobwetrust.kintamanga.util.extension.toast
import io.github.innoobwetrust.kintamanga.util.extension.uriString
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import rx.Subscription
import timber.log.Timber
import java.io.InputStream

class MangaInfoActivity :
        AppCompatActivity(),
        KodeinGlobalAware,
        MangaInfoLoader,
        ChapterInteractionListener,
        MaterialCab.Callback {
    companion object {
        // Intent keys
        enum class Intents(val key: String) {
            MANGA_INFO("MANGA_INFO_INTENT_KEY"),
            CHAPTER_INDEX("CHAPTER_INDEX_INTENT_KEY")
        }

        // SaveInstanceState keys
        private enum class SavedInstanceStates(val key: String) {
            MANGA("MANGA_SAVE_KEY"),
            SELECTED_INDICES("SELECTED_INDICES_SAVE_KEY")
        }
    }

    private var cab: MaterialCab? = null
    val mangaSourceName: String
        get() = mangaBinding.mangaSourceName
    override val mangaInfoProcessor: MangaInfoProcessor
        get() = SourceManager.getMangaInfoProcessorForSourceName(sourceName = mangaSourceName)!!

    override lateinit var mangaBinding: MangaBinding
    override var refreshDisposable: Subscription? = null
    override var observeDownloaderDisposable: Subscription? = null

    private lateinit var binding: ActivityMangaInfoBinding
    private var isFABOpen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null != savedInstanceState) {
            mangaBinding = savedInstanceState.getSerializable(
                    SavedInstanceStates.MANGA.key
            ) as? MangaBinding ?: throw Exception("Error! mangaBinding not saved properly")
        }
        if (null == savedInstanceState) {
            mangaBinding = intent.getSerializableExtra(Intents.MANGA_INFO.key) as? MangaBinding ?:
                    MangaBinding().apply {
                        Timber.v(intent.dataString)
                        mangaSourceName = SourceManager
                                .findFirstSourceNameForHost(intent?.data?.host) ?:
                                throw Exception("Error processing deep link! Can not find source")
                        mangaUri = SourceManager.normalizeUri(intent?.data ?: Uri.EMPTY)?.uriString ?:
                                throw Exception("Error processing deep link! Can not normalize uri")
                    }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manga_info)
        bind()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.listChapters.let {
            it.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            it.adapter = ChapterListAdapter(
                    mangaInfoActivity = this,
                    mangaBinding = mangaBinding,
                    selectedIndices = savedInstanceState
                            ?.getString(SavedInstanceStates.SELECTED_INDICES.key)
                            ?.run { instance<Gson>().fromJson(this) }
                            ?: mutableSetOf()
            )
        }
        binding.mangaMenuFab.setOnClickListener(menuFabOnClickListener)
        cab = MaterialCab.restoreState(savedInstanceState, this, this)
    }

    override fun onResume() {
        super.onResume()
        Glide.get(applicationContext).registry.replace(
                GlideUrl::class.java,
                InputStream::class.java,
                OkHttpUrlLoader.Factory(
                    instance<OkHttpClient>("cover")
                        .newBuilder()
                        .addInterceptor(factory<Headers, Interceptor>("headers")(mangaInfoProcessor.headers()))
                        .build()
                )
        )
        if (mangaBinding.chapters.isEmpty()) {
            backgroundRefresh(
                    onRefreshSuccess = onRefreshSuccess,
                    onError = onRefreshError
            )
        } else {
            onRefreshSuccess(true)
        }
        observeDownloads(
                onDownloadStatusChange = onDownloadStatusChange,
                onError = onObservableDownloadError
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.let {
            it.putSerializable(SavedInstanceStates.MANGA.key, mangaBinding)
            (binding.listChapters.adapter as? ChapterListAdapter)?.selectedIndices?.run {
                it.putString(
                        SavedInstanceStates.SELECTED_INDICES.key,
                        instance<Gson>().typedToJson(this)
                )
            }
            cab?.saveState(it)
        }
    }

    override fun onPause() {
        disposeAllLoaderDisposables()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mangaBinding = null
        binding.executePendingBindings()
        binding.headerMangaInfo.executePendingBindings()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        (binding.listChapters.adapter as? ChapterListAdapter)?.let {
            if (it.selectedIndices.isNotEmpty()) {
                it.clearSelected()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onCabCreated(cab: MaterialCab?, menu: Menu?): Boolean {
        return true
    }

    override fun onCabItemClicked(item: MenuItem?): Boolean {
        (binding.listChapters.adapter as? ChapterListAdapter)?.let {
            // Because chapter index is ordered in revert, we need to fix the list of indices
            val size = mangaBinding.chapters.size
            val chapterSelectedIndices = it.selectedIndices.map { index -> size - index - 1 }
            when (item?.itemId) {
                R.id.select_all -> it.selectAll()
                R.id.invert_selection -> it.invertSelection()
                R.id.remove_chapters -> onDeleteRequest(
                        mangaBinding.chapters.filter { chapter ->
                            chapter.chapterIndex in chapterSelectedIndices
                        },
                        true
                )
                R.id.download_chapters -> onDownloadRequest(
                        mangaBinding.chapters.filter { chapter ->
                            chapter.chapterIndex in chapterSelectedIndices
                        },
                        showDialog = true
                )
                R.id.toggle_chapters_read_status ->
                    mangaBinding.chapters.filter { chapter ->
                        chapter.chapterIndex in chapterSelectedIndices
                    }.onEach { chapter ->
                        chapter.chapterViewed = !chapter.chapterViewed
                    }.let { chapters -> onReadStatusToggled(chapters) }
            }
        }
        return true
    }

    override fun onCabFinished(cab: MaterialCab?): Boolean {
        (binding.listChapters.adapter as? ChapterListAdapter)?.clearSelected()
        return true
    }

    private fun bind() {
        binding.mangaBinding = mangaBinding
        binding.executePendingBindings()
        binding.headerMangaInfo.executePendingBindings()
    }

    private fun showFABMenu() {
        isFABOpen = true
        binding.mangaClearFab.animate()?.translationX(-resources.getDimension(R.dimen.standard_305))
        binding.mangaShareFab.animate()?.translationX(-resources.getDimension(R.dimen.standard_205))
        binding.mangaFavoriteFab.animate()?.translationX(-resources.getDimension(R.dimen.standard_105))
        binding.mangaMenuFab.setImageResource(R.drawable.ic_cancel_white_24dp)
    }

    private fun closeFABMenu() {
        isFABOpen = false
        binding.mangaClearFab.animate()?.translationX(0f)
        binding.mangaShareFab.animate()?.translationX(0f)
        binding.mangaFavoriteFab.animate()?.translationX(0f)
        binding.mangaMenuFab.setImageResource(R.drawable.ic_menu_white_24dp)
    }

    private val menuFabOnClickListener: (View) -> Unit = { _ ->
        if (!isFABOpen) {
            showFABMenu()
        } else {
            closeFABMenu()
        }
    }

    private val favoriteFabOnClickListener: (View) -> Unit = { _ ->
        mangaBinding.mangaFavorited = !mangaBinding.mangaFavorited
        if (!mangaBinding.mangaFavorited) backgroundRemoveOrUpdateMangaDb()
        else backgroundSaveFullInfo()
    }

    private val shareFabOnClickListener: (View) -> Unit = { _ ->
        val sharingIntent = Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, mangaBinding.mangaUri)
                .setType("text/plain")
        startActivity(
                Intent.createChooser(
                        sharingIntent,
                        getString(R.string.manga_share_chooser_title)
                )
        )
    }

    private val mangaClearFabOnClickListener: (View) -> Unit = { _ ->
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.clean_manga_dialog_title))
                .setMessage(getString(R.string.clean_manga_dialog_message))
                .setPositiveButton(
                        getString(R.string.clean_manga_dialog_positive_button_text)
                ) { _, _ ->
                    disposeAllLoaderDisposables()
                    val deletingDialog = AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(R.string.clean_manga_progress_dialog_message)
                            .create()
                    deletingDialog.show()
                    backgroundRemoveAllDownloads(
                            onComplete = {
                                deletingDialog.dismiss()
                                observeDownloads(
                                        onDownloadStatusChange = onDownloadStatusChange,
                                        onError = onObservableDownloadError
                                )
                            },
                            onError = { error ->
                                toast(R.string.remove_download_failed_text)
                                Timber.e(error)
                            }
                    )
                }
                .setNegativeButton(
                        getString(R.string.clean_manga_dialog_negative_button_text),
                        null
                )
                .show()
    }

    private val onRefreshSuccess: (Boolean) -> Unit = { success ->
        binding.progress.visibility = View.GONE
        binding.mangaClearFab.setOnClickListener(mangaClearFabOnClickListener)
        binding.mangaShareFab.setOnClickListener(shareFabOnClickListener)
        binding.mangaFavoriteFab.setOnClickListener(favoriteFabOnClickListener)
        if (success) {
            binding.listChapters.adapter?.notifyDataSetChanged()
        } else {
            toast(R.string.manga_info_refresh_failed_text)
        }
    }

    private val onRefreshError: (Throwable) -> Unit = { error ->
        toast(R.string.manga_info_refresh_error_text)
        Timber.e(error)
    }

    private val onDownloadStatusChange: (Download) -> Unit = { download ->
        mangaBinding.chapters
                .find { it.chapterUri == download.chapter.chapterUri }
                ?.let {
                    it.chapterDownloadStatus = download.downloadStatus
                    if (DownloadStatus.DOWNLOADED == download.downloadStatus) {
                        mangaBinding.mangaDownloaded = true
                    }
                    Timber.v(download.downloadStatus.name)
                }
                ?: Exception("Can not find chapter: ${download.chapter.chapterUri}").let {
            Timber.e(it)
        }
    }

    private val onObservableDownloadError: (Throwable) -> Unit = { error ->
        toast(R.string.observe_download_failed_text)
        Timber.e(error)
    }

    @Throws(Exception::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Glide.get(applicationContext).registry.replace(
                GlideUrl::class.java,
                InputStream::class.java,
                OkHttpUrlLoader.Factory(
                    instance<OkHttpClient>("cover")
                        .newBuilder()
                        .addInterceptor(factory<Headers, Interceptor>("headers")(mangaInfoProcessor.headers()))
                        .build()
                )
        )
        if (0 == requestCode && Activity.RESULT_OK == resultCode && null != data) {
            val updatedMangaBinding =
                    data.getSerializableExtra(Intents.MANGA_INFO.key) as? MangaBinding ?:
                            throw Exception("Reader return invalid result!")
            mangaBinding.copyFrom(updatedMangaBinding)
            updatedMangaBinding.chapters.forEach { updatedChapterBinding ->
                mangaBinding.chapters
                        .find { it.chapterIndex == updatedChapterBinding.chapterIndex }
                        ?.copyFrom(updatedChapterBinding)
            }
            if (null != mangaBinding.id) backgroundSaveFullInfo()
        }
    }

    override fun onLongClick(index: Int) {
        binding.listChapters.setDragSelectActive(true, index)
    }

    override fun onSelectionChanged(count: Int) {
        if (count > 0) {
            if (cab == null) {
                cab = MaterialCab(this, R.id.cab_stub)
                        .setMenu(R.menu.menu_cab_manga_info)
                        .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                        .start(this)
                        .also {
                            it.toolbar?.setTitleTextColor(
                                    ContextCompat.getColor(this, R.color.colorTextTitle)
                            )
                        }
            }
            cab?.setTitleRes(R.string.chapters_select_cab_title, count)
        } else {
            cab?.let {
                if (it.isActive)
                    it.reset().finish()
            }.also { cab = null }
        }
    }

    override fun onChapterClick(chapterBinding: ChapterBinding) {
        if (View.VISIBLE == binding.progress.visibility) return
        val readerIntent: Intent =
                Intent(this, ReaderActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(Intents.MANGA_INFO.key, mangaBinding)
                        .putExtra(Intents.CHAPTER_INDEX.key, chapterBinding.chapterIndex)
        startActivityForResult(readerIntent, 0)
    }

    private fun downloadChapters(chapterBindings: List<ChapterBinding>) {
        backgroundPrepareForDownload(
                chapterBindings = chapterBindings,
                onSuccess = { success ->
                    if (success && chapterBindings.none { null == it.id }) {
                        DownloadService.start(this)
                        instance<Downloader>().queueChapters(mangaBinding, chapterBindings)
                    } else throw Exception("manga or chapters not saved properly to database")
                },
                onError = { error ->
                    toast(R.string.download_request_prepare_failed_text)
                    Timber.e(error)
                }
        )
        chapterBindings.forEach { it.chapterDownloadStatus = DownloadStatus.QUEUE }
    }

    override fun onDownloadRequest(
            chapterBindings: List<ChapterBinding>,
            showDialog: Boolean
    ) {
        if (showDialog) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.download_chapters_dialog_title))
                    .setMessage(getString(R.string.download_chapters_dialog_message))
                    .setPositiveButton(
                            getString(R.string.download_chapters_dialog_positive_button_text)
                    ) { _, _ -> downloadChapters(chapterBindings) }
                    .setNegativeButton(
                            getString(R.string.download_chapters_dialog_negative_button_text),
                            null
                    )
                    .show()
        } else {
            downloadChapters(chapterBindings)
        }
    }

    private fun removeChapters(chapters: List<ChapterBinding>) =
            backgroundRemoveDownloadedChapters(
                    chapterBindings = chapters,
                    onComplete = null,
                    onError = { error ->
                        toast(R.string.remove_downloaded_chapter_failed_text)
                        Timber.e(error)
                    }
            )

    override fun onDeleteRequest(
            chapterBindings: List<ChapterBinding>,
            showDialog: Boolean
    ) {
        if (showDialog) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_chapters_dialog_title))
                    .setMessage(getString(R.string.delete_chapters_dialog_message))
                    .setPositiveButton(
                            getString(R.string.delete_chapters_dialog_positive_button_text)
                    ) { _, _ -> removeChapters(chapterBindings) }
                    .setNegativeButton(
                            getString(R.string.delete_chapters_dialog_negative_button_text),
                            null
                    )
                    .show()
        } else {
            removeChapters(chapterBindings)
        }
    }

    override fun onReadStatusToggled(chapterBindings: List<ChapterBinding>) {
        backgroundSaveChapters(chapterBindings)
    }
}
