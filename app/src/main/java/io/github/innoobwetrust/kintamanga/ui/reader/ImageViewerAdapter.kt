package io.github.innoobwetrust.kintamanga.ui.reader

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import kotlinx.android.synthetic.main.holder_image_viewer.view.*
import java.io.File

class ImageViewerAdapter(
        private var viewer: ViewerFragment?,
        private var chapterBinding: ChapterBinding?,
        private val viewerType: ViewerTypes
) : RecyclerView.Adapter<ImageViewerAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_image_viewer, parent, false)
        if (viewerType == ViewerTypes.WEBTOON) {
            inflatedView.imageViewerLayout?.layoutParams?.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT
        }
        return ViewHolder(inflatedView = inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        chapterBinding?.let { binding ->
            holder.setImage(binding.chapterPages[position])
            holder.touchOverlay.setOnClickListener {
                (viewer?.activity as? ViewerFragmentListener)
                        ?.onViewerToggleControl()
            }
            holder.failureImage.setOnClickListener {
                (viewer?.activity as? ViewerFragmentListener)
                        ?.onViewerToggleControl()
            }
            holder.reloadImageButton.setOnClickListener {
                holder.touchOverlay.visibility = View.VISIBLE
                holder.failureView.visibility = View.GONE
                holder.setImage(binding.chapterPages[position])
            }
            holder.chapterImageView.ssiv.setOnTouchListener { _, motionEvent ->
                viewer?.gestureDetector?.onTouchEvent(motionEvent) ?: true
            }
        }
    }

    override fun getItemCount(): Int {
        return chapterBinding?.chapterPages?.size ?: 0
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        viewer = null
        chapterBinding = null
        System.gc()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.let {
            it.touchOverlay.setOnClickListener(null)
            it.failureImage.setOnClickListener(null)
            it.reloadImageButton.setOnClickListener(null)
            it.chapterImageView.setOnClickListener(null)
            it.chapterImageView.ssiv.setOnTouchListener(null)
            it.chapterImageView.ssiv.recycle()
        }
        System.gc()
        super.onViewRecycled(holder)
    }

    inner class ViewHolder(
            inflatedView: View
    ) : RecyclerView.ViewHolder(inflatedView) {
        val chapterImageView: BigImageView = inflatedView.chapterImage
        val touchOverlay: View = inflatedView.touchOverlay
        val failureView: ConstraintLayout = inflatedView.failureView
        val failureImage: AppCompatImageView = inflatedView.failureImage
        val reloadImageButton: AppCompatButton = inflatedView.reloadImageButton

        init {
            chapterImageView.apply {
                ssiv.setMinimumDpi(90)
                ssiv.setMinimumTileDpi(180)
                ssiv.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
                ssiv.setOnImageEventListener(
                        object : SubsamplingScaleImageView.OnImageEventListener {
                            override fun onReady() {}
                            override fun onTileLoadError(p0: Exception?) {}
                            override fun onPreviewReleased() {}
                            override fun onPreviewLoadError(p0: Exception?) {}
                            override fun onImageLoaded() {
                                this@ViewHolder.touchOverlay.visibility = View.GONE
                                this@ViewHolder.failureView.visibility = View.GONE
                            }

                            override fun onImageLoadError(p0: Exception?) {
                                this@ViewHolder.touchOverlay.visibility = View.VISIBLE
                                this@ViewHolder.failureView.visibility = View.VISIBLE
                                onFail(Exception("Failed to load image from file"))
                            }
                        }
                )
                setImageLoaderCallback(object : ImageLoader.Callback {
                    override fun onFinish() {}
                    override fun onCacheHit(image: File?) {}
                    override fun onCacheMiss(image: File?) {}
                    override fun onProgress(progress: Int) {}
                    override fun onStart() {}
                    override fun onSuccess(image: File?) {}
                    override fun onFail(error: java.lang.Exception?) {
                        this@ViewHolder.touchOverlay.visibility = View.VISIBLE
                        this@ViewHolder.failureView.visibility = View.VISIBLE
                    }
                })
                setProgressIndicator(ProgressPieIndicator())
            }
        }

        fun setImage(page: Page) {
            chapterImageView.apply {
                Glide.with(this).clear(this)
                ssiv.recycle()
                if (!page.imageFileUri.isBlank()) {
                    showImage(Uri.parse(page.imageFileUri))
                } else if (!page.imageUrls[viewer!!.serverIndex!!].isBlank()) {
                    showImage(Uri.parse(page.imageUrls[viewer!!.serverIndex!!]))
                }
            }
        }
    }
}
