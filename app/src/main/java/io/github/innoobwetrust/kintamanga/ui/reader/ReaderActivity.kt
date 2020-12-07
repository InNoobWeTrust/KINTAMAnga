package io.github.innoobwetrust.kintamanga.ui.reader

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.ActivityReaderBinding
import io.github.innoobwetrust.kintamanga.source.SourceManager
import io.github.innoobwetrust.kintamanga.source.processor.ChapterInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.manga.MangaInfoActivity
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.toast
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ReaderActivity :
        AppCompatActivity(),
        KodeinGlobalAware,
        ChapterInfoLoader,
        ViewerFragmentListener {
    companion object {

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000L

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 150L

        // Fragment tag
        private const val CONTENT_VIEW_FRAGMENT_TAG = "CONTENT_VIEW_FRAGMENT_TAG"

        // SaveInstanceState keys
        private enum class SavedInstanceStates(val key: String) {
            MANGA("MANGA_SAVE_KEY"),
            CHAPTER_INDEX("CHAPTER_INDEX_SAVE_KEY")
        }
    }

    private var mContentFragment: Fragment? = null

    val mangaSourceName: String
        get() = mangaBinding.mangaSourceName
    override val chapterInfoProcessor: ChapterInfoProcessor
        get() = SourceManager.getChapterInfoProcessorForSourceName(sourceName = mangaSourceName)!!

    override lateinit var mangaBinding: MangaBinding
    override var chapterIndex: Int = -1
    var serverIndex = 0
        set(value) {
            field = value
            doFragmentTransaction()
        }
    private var numServer: Int = 1
        set(value) {
            field = value
            setupServerChooser()
        }

    override var loadChapterDisposable: Subscription? = null

    private var startPage: Int? = null
    private var timerDisposable: Subscription? = null

    private lateinit var binding: ActivityReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == savedInstanceState) {
            mangaBinding = intent.getSerializableExtra(
                    MangaInfoActivity.Companion.Intents.MANGA_INFO.key
            ) as? MangaBinding ?: throw Exception("Error! mangaBinding not passed to intent")
            chapterIndex = intent.getIntExtra(
                    MangaInfoActivity.Companion.Intents.CHAPTER_INDEX.key,
                    0
            )
        }
        if (null != savedInstanceState) {
            mangaBinding = savedInstanceState.getSerializable(
                    SavedInstanceStates.MANGA.key
            ) as? MangaBinding ?: throw Exception("Error! mangaBinding not saved properly")
            chapterIndex = savedInstanceState.getInt(SavedInstanceStates.CHAPTER_INDEX.key)
        }
        binding =
                DataBindingUtil.setContentView(this, R.layout.activity_reader)
        binding.mangaBinding = mangaBinding
        binding.executePendingBindings()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.fullscreenContent.setOnClickListener { toggle() }
        mVisible = true
        setupChapterNavigationButtons()
        binding.previousChapterButton.setOnClickListener {
            --chapterIndex
            reloadViewer()
            startPage = -1
            hide(AUTO_HIDE_DELAY_MILLIS)
        }
        binding.nextChapterButton.setOnClickListener {
            ++chapterIndex
            reloadViewer()
            startPage = 0
            hide(AUTO_HIDE_DELAY_MILLIS)
        }
        binding.chapterSelectButton.setOnClickListener {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setItems(
                            mangaBinding.chapters.map { it.chapterTitle }.toTypedArray()
                    ) { _: DialogInterface?, i: Int ->
                        chapterIndex = i
                        reloadViewer()
                        startPage = 0
                        hide(AUTO_HIDE_DELAY_MILLIS)
                    }
                    .create()
                    .show()
        }
        binding.changeViewerTypeButton.setOnClickListener {
            if ((0..(ViewerTypes.values().size - 2)).contains(mangaBinding.mangaViewer)) {
                ++mangaBinding.mangaViewer
            } else {
                mangaBinding.mangaViewer = 0
            }
            doFragmentTransaction()
            onViewerTypeChanged()
            hide(AUTO_HIDE_DELAY_MILLIS)
        }
        binding.pageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean) {
                val max = seekBar?.max
                if (null != max) {
                    if (fromUser) {
                        (mContentFragment as? ViewerFragment)?.syncPosition(
                                newPosition = progress,
                                progressFeedback = false
                        )
                        hide(AUTO_HIDE_DELAY_MILLIS)
                    }
                    binding.pageIndicator.text =
                            getString(R.string.reader_page_indicator_text, progress + 1, max + 1)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    override fun onResume() {
        super.onResume()
        doFragmentTransaction()
        if (chapterBinding.chapterPages.isEmpty()) {
            backgroundRefresh(
                    context = this,
                    onChapterLoaded = onChapterLoaded,
                    onChapterLoadError = onChapterLoadError
            )
        } else {
            onChapterLoaded(true)
        }
    }

    override fun onBackPressed() {
        val resultIntent = Intent().putExtra(
                MangaInfoActivity.Companion.Intents.MANGA_INFO.key,
                mangaBinding
        )
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
//            // This ID represents the Home or Up button.
//            NavUtils.navigateUpFromSameTask(this)
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putSerializable(SavedInstanceStates.MANGA.key, mangaBinding)
            putInt(SavedInstanceStates.CHAPTER_INDEX.key, chapterIndex)
        }
    }

    override fun onPause() {
        disposeAllLoaderDisposables()
        timerDisposable?.run { if (!isUnsubscribed) unsubscribe() }
        mContentFragment?.let {
            supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
        }
        mContentFragment = null
        System.gc()
        super.onPause()
    }

    private fun doFragmentTransaction() {
        val viewerFragment = ViewerFragment.newInstance()
        mContentFragment = viewerFragment
        supportFragmentManager
                .beginTransaction()
                .replace(binding.fullscreenContent.id, viewerFragment, CONTENT_VIEW_FRAGMENT_TAG)
                .commitNowAllowingStateLoss()
    }

    private fun setupChapterNavigationButtons() {
        if (chapterIndex > 0) {
            binding.previousChapterButton.visibility = View.VISIBLE
        } else {
            binding.previousChapterButton.visibility = View.INVISIBLE
        }
        if (chapterIndex < mangaBinding.chapters.size - 1) {
            binding.nextChapterButton.visibility = View.VISIBLE
        } else {
            binding.nextChapterButton.visibility = View.INVISIBLE
        }
    }

    private fun setupServerChooser() {
        val chapterServerChooserAdapter = ArrayAdapter(
                this,
                R.layout.themed_spinner_item,
                (1..numServer)
                        .map {
                            getString(R.string.reader_server_picker_text, it)
                        }
                        .toList()
        )
        chapterServerChooserAdapter.setDropDownViewResource(R.layout.themed_spinner_dropdown_item)
        binding.chapterServerChooser.adapter = chapterServerChooserAdapter
        binding.chapterServerChooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                serverIndex = position
            }
        }
    }

    private fun onViewerTypeChanged() {
        binding.changeViewerTypeButton.setImageResource(
                when (mangaBinding.mangaViewer) {
                    ViewerTypes.PAGER_HORIZONTAL_LEFT_TO_RIGHT.ordinal,
                    ViewerTypes.PAGER_HORIZONTAL_RIGHT_TO_LEFT.ordinal ->
                        R.drawable.ic_view_carousel_white_24dp
                    ViewerTypes.PAGER_VERTICAL.ordinal, ViewerTypes.WEBTOON.ordinal ->
                        R.drawable.ic_view_day_white_24dp
                    else -> R.drawable.ic_view_carousel_white_24dp
                }
        )
        binding.viewerTypeIndicator.setImageResource(
                when (mangaBinding.mangaViewer) {
                    ViewerTypes.PAGER_HORIZONTAL_LEFT_TO_RIGHT.ordinal ->
                        if (ViewCompat.LAYOUT_DIRECTION_RTL
                                == ViewCompat.getLayoutDirection(binding.viewerTypeIndicator)) {
                            R.drawable.ic_arrow_back_white_24dp
                        } else {
                            R.drawable.ic_arrow_forward_white_24dp
                        }
                    ViewerTypes.PAGER_HORIZONTAL_RIGHT_TO_LEFT.ordinal ->
                        if (ViewCompat.LAYOUT_DIRECTION_RTL
                                == ViewCompat.getLayoutDirection(binding.viewerTypeIndicator)) {
                            R.drawable.ic_arrow_forward_white_24dp
                        } else {
                            R.drawable.ic_arrow_back_white_24dp
                        }
                    ViewerTypes.PAGER_VERTICAL.ordinal, ViewerTypes.WEBTOON.ordinal ->
                        R.drawable.ic_arrow_downward_white_24dp
                    else -> R.drawable.ic_arrow_forward_white_24dp
                }
        )
        binding.viewerTypeIndicatorCard.visibility = View.VISIBLE
        timerDisposable?.run { if (!isUnsubscribed) unsubscribe() }
        timerDisposable = Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { binding.viewerTypeIndicatorCard.visibility = View.GONE }
    }

    private fun reloadViewer() {
        setupChapterNavigationButtons()
        binding.chapterStatusAndTitle.text = getText(R.string.chapter_loading_text)
        backgroundRefresh(
                context = this,
                onChapterLoaded = onChapterLoaded,
                onChapterLoadError = onChapterLoadError
        )
    }

    private val onChapterLoaded: (Boolean) -> Unit = { success ->
        if (success) {
            if (chapterBinding.chapterPages.isEmpty()) {
                binding.chapterStatusAndTitle.text = getText(R.string.chapter_load_empty_text)
                toast(R.string.chapter_load_empty_text)
            } else {
                onViewerTypeChanged()
                binding.chapterStatusAndTitle.text = chapterBinding.chapterTitle
                binding.chapterCommentFab.setOnClickListener {
                    val commentIntent = Intent(Intent.ACTION_VIEW, Uri.parse(chapterBinding.chapterUri))
                    startActivity(commentIntent)
                }
                binding.pageSeekBar.max = chapterBinding.chapterPages.size - 1
                when (startPage) {
                    -1 -> chapterBinding.chapterLastPageRead = chapterBinding.chapterPages.size - 1
                    0 -> chapterBinding.chapterLastPageRead = 0
                }
                chapterBinding.chapterViewed = true
                numServer = chapterBinding
                        .chapterPages
                        .firstOrNull()
                        ?.imageUrls
                        ?.size
                        ?: 1
                Timber.v("number of servers: $numServer")
                hide(1000)
            }
        } else {
            binding.chapterStatusAndTitle.text = getText(R.string.chapter_load_failed_text)
            toast(R.string.chapter_load_failed_text)
        }
    }
    private val onChapterLoadError: (Throwable) -> Unit = { error ->
        binding.chapterStatusAndTitle.text = getText(R.string.chapter_load_error_text)
        toast(R.string.chapter_load_error_text)
        Timber.e(error)
    }

    override fun onViewerPageChanged(newPagePosition: Int) {
        binding.pageSeekBar.progress = newPagePosition
    }

    override fun onTapPreviousPage() {
        (mContentFragment as? ViewerFragment)
                ?.syncPosition(newPosition = chapterBinding.chapterLastPageRead - 1)
        Timber.v("Image tapped for previous page")
    }

    override fun onTapNextPage() {
        (mContentFragment as? ViewerFragment)
                ?.syncPosition(newPosition = chapterBinding.chapterLastPageRead + 1)
        Timber.v("Image tapped for previous page")
    }

    override fun onViewerToggleControl() = toggle()

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        binding.fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        supportActionBar?.hide()
        binding.fullscreenStatus.visibility = View.GONE
        binding.fullscreenContentControls.visibility = View.GONE
        binding.chapterCommentFab.hide()
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        binding.fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        supportActionBar?.show()
        binding.fullscreenStatus.visibility = View.VISIBLE
        binding.mangaTitle.isSelected = true
        binding.chapterStatusAndTitle.isSelected = true
        binding.fullscreenContentControls.visibility = View.VISIBLE
        binding.chapterCommentFab.show()
    }

    private var mVisible: Boolean = false

    private fun toggle() = when (mVisible) {
        true -> hide()
        false -> show()
    }

    private fun hide(delay: Long = UI_ANIMATION_DELAY) {
        mVisible = false
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, delay)
    }

    @SuppressLint("InlinedApi")
    private fun show(delay: Long = UI_ANIMATION_DELAY) {
        // Show the system bar
        mVisible = true
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, delay)
    }
}
