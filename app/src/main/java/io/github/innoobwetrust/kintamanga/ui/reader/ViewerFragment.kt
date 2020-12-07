package io.github.innoobwetrust.kintamanga.ui.reader

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.util.ViewPreloadSizeProvider
import io.github.innoobwetrust.kintamanga.databinding.FragmentImageViewerBinding
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import java.io.File
import kotlin.math.abs

class ViewerFragment : Fragment(), ReaderActivityListener {

    companion object {
        fun newInstance(): ViewerFragment = ViewerFragment()
    }

    private var mReader: ReaderActivity? = null
    private val mViewerType: Int?
        get() = mReader?.mangaBinding?.mangaViewer
    private val chapterBinding: ChapterBinding?
        get() = mReader?.chapterBinding
    private val glideHeaders: Headers?
        get() = (activity as? ReaderActivity)
                ?.chapterInfoProcessor
                ?.headers()
                ?.fold(
                        LazyHeaders.Builder(),
                        { builder: LazyHeaders.Builder, pair: Pair<String, String> ->
                            builder.addHeader(pair.first, pair.second)
                        })
                ?.build()
    val serverIndex: Int
        get() = mReader?.serverIndex ?: 0
    private val pagerSnapHelper: PagerSnapHelper by lazy {
        object : PagerSnapHelper() {
            override fun findTargetSnapPosition(
                    layoutManager: RecyclerView.LayoutManager?,
                    velocityX: Int,
                    velocityY: Int
            ): Int {
                val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
                position.coerceIn(0..(chapterBinding?.chapterPages?.size ?: 0)).let {
                    if (it != chapterBinding?.chapterLastPageRead)
                        mReader?.onViewerPageChanged(newPagePosition = position)
                    chapterBinding?.chapterLastPageRead = position
                }
                return position
            }
        }
    }

    val gestureDetector: GestureDetector by lazy {
        GestureDetector(
                this.context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        this@ViewerFragment.view?.let {
                            if (ViewerTypes.WEBTOON.ordinal == mViewerType) {
                                (activity as? ViewerFragmentListener)
                                        ?.onViewerToggleControl()
                                return true
                            }
                            val pointX = e.x
                            val pointY = e.y
                            var horizontalTap = 0
                            var verticalTap = 0
                            if (pointX < it.width * 0.2f) {
                                horizontalTap = -1
                            } else if (pointX > it.width * 0.8f) {
                                horizontalTap = 1
                            }
                            if (pointY < it.height * 0.15f) {
                                verticalTap = -1
                            } else if (pointY > it.height * 0.85f) {
                                verticalTap = 1
                            }
                            when (mViewerType) {
                                ViewerTypes.PAGER_HORIZONTAL_LEFT_TO_RIGHT.ordinal ->
                                    when (horizontalTap) {
                                        -1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapPreviousPage()
                                        1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapNextPage()
                                        else -> (activity as? ViewerFragmentListener)
                                                ?.onViewerToggleControl()
                                    }
                                ViewerTypes.PAGER_HORIZONTAL_RIGHT_TO_LEFT.ordinal ->
                                    when (horizontalTap) {
                                        -1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapNextPage()
                                        1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapPreviousPage()
                                        else -> (activity as? ViewerFragmentListener)
                                                ?.onViewerToggleControl()
                                    }
                                ViewerTypes.PAGER_VERTICAL.ordinal ->
                                    when (verticalTap) {
                                        -1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapPreviousPage()
                                        1 -> (activity as? ViewerFragmentListener)
                                                ?.onTapNextPage()
                                        else -> (activity as? ViewerFragmentListener)
                                                ?.onViewerToggleControl()
                                    }
                                else -> (activity as? ViewerFragmentListener)
                                        ?.onViewerToggleControl()
                            }
                        }
                        return true
                    }
                })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ReaderActivity) {
            mReader = context
        } else {
            throw RuntimeException("this fragment can only be used by ReaderActivity")
        }
    }

    private lateinit var binding: FragmentImageViewerBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Always re-create RecyclerVew to sync position, this won't require much computation
        binding = FragmentImageViewerBinding.inflate(inflater, container, false)
        binding.imageViewerRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = when (mViewerType) {
                ViewerTypes.PAGER_HORIZONTAL_LEFT_TO_RIGHT.ordinal ->
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                ViewerTypes.PAGER_HORIZONTAL_RIGHT_TO_LEFT.ordinal ->
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
                ViewerTypes.WEBTOON.ordinal ->
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                else -> LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            if (ViewerTypes.WEBTOON.ordinal != mViewerType) {
                pagerSnapHelper.attachToRecyclerView(this)
            }
            adapter = ImageViewerAdapter(
                    viewer = this@ViewerFragment,
                    chapterBinding = chapterBinding!!,
                    glideHeaders = glideHeaders,
                    viewerType = ViewerTypes.values()[mViewerType ?: 0]
            )
            // Sync position
            if (ViewerTypes.WEBTOON.ordinal == mViewerType) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                            chapterBinding?.let {
                                it.chapterLastPageRead =
                                        (layoutManager as? LinearLayoutManager)
                                                ?.findLastVisibleItemPosition() ?: throw Exception(
                                                "Error! Can't cast layoutManager to LinearLayoutManager"
                                        )
                                mReader?.onViewerPageChanged(
                                        newPagePosition = it.chapterLastPageRead
                                )
                            }
                        }
                    }
                })
                addOnScrollListener(RecyclerViewPreloader(Glide.with(this), object : ListPreloader.PreloadModelProvider<Page> {
                    override fun getPreloadItems(position: Int): List<Page> {
                        return chapterBinding?.chapterPages?.get(position)?.run { listOf(this) }
                                ?: emptyList()
                    }

                    override fun getPreloadRequestBuilder(page: Page): RequestBuilder<File> {
                        return Glide.with(this@ViewerFragment).downloadOnly().load(when {
                            page.imageFileUri.isNotBlank() -> {
                                Uri.parse(page.imageFileUri)
                            }
                            page.imageUrls[this@ViewerFragment.serverIndex].isNotBlank() -> {
                                Uri.parse(page.imageUrls[this@ViewerFragment.serverIndex])
                            }
                            else -> {
                                Uri.EMPTY
                            }
                        })
                    }
                }, ViewPreloadSizeProvider(), 10))
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        syncPosition(newPosition = null)
    }

    override fun onDestroyView() {
        binding.imageViewerRecyclerView.adapter = null
        super.onDestroyView()
        System.gc()
    }

    override fun onDetach() {
        mReader = null
        binding.imageViewerRecyclerView.adapter = null
        super.onDetach()
        System.gc()
    }

    fun syncPosition(newPosition: Int?, progressFeedback: Boolean = true) {
        chapterBinding?.let {
            if (it.chapterPages.isEmpty()) return
            var pagesJump = 0
            if (null != newPosition) {
                val coercedNewPosition = newPosition.coerceIn(it.chapterPages.indices)
                pagesJump = abs(coercedNewPosition - it.chapterLastPageRead)
                it.chapterLastPageRead = coercedNewPosition
            }
            if (progressFeedback) {
                mReader?.onViewerPageChanged(newPagePosition = it.chapterLastPageRead)
            }
            if (pagesJump == 1)
                binding.imageViewerRecyclerView.smoothScrollToPosition(it.chapterLastPageRead)
            else
                binding.imageViewerRecyclerView.scrollToPosition(it.chapterLastPageRead)
        }
    }

    override fun onImageLoaded(position: Int) {
        binding.imageViewerRecyclerView.adapter?.notifyItemChanged(position)
    }
}
