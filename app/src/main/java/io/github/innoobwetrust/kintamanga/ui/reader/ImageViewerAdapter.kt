package io.github.innoobwetrust.kintamanga.ui.reader

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.HolderImageViewerBinding
import io.github.innoobwetrust.kintamanga.model.Page
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import java.io.File

class ImageViewerAdapter(
        private var viewer: ViewerFragment?,
        private var chapterBinding: ChapterBinding?,
        private val glideHeaders: Headers?,
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
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HolderImageViewerBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        chapterBinding?.let { binding ->
            if (viewerType == ViewerTypes.WEBTOON) {
                holder.binding.imageViewerLayout.layoutParams.height =
                        ViewGroup.LayoutParams.WRAP_CONTENT
            }
            holder.bind(binding.chapterPages[position], viewer?.serverIndex
                    ?: 0)
            holder.binding.chapterImage.apply {
                setOnTouchListener { _, motionEvent ->
                    viewer?.view?.performClick()
                    viewer?.gestureDetector?.onTouchEvent(motionEvent) ?: true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chapterBinding?.chapterPages?.size ?: 0
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        viewer = null
        chapterBinding = null
//        System.gc()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.binding.apply {
            chapterImage.setOnClickListener(null)
            chapterImage.recycle()
        }
//        System.gc()
        super.onViewRecycled(holder)
    }

    inner class ViewHolder(
            val binding: HolderImageViewerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var page: Page

        fun bind(page: Page, serverIndex: Int) {
            this.page = page
            binding.chapterImage.apply {
                recycle()
                setMinimumDpi(90)
                setMinimumTileDpi(180)
                setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
                setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
            }
            if (page.imageFileUri.isBlank() && page.imageUrls[serverIndex].isBlank()) {
                binding.chapterImage.setImage(ImageSource.resource(R.drawable.broken_image_white_192x192))
                return
            }
            var loader = Glide.with(binding.chapterImage).downloadOnly()
            when {
                page.imageFileUri.isNotBlank() -> loader = loader.load(page.imageFileUri)
                page.imageUrls[serverIndex].isNotBlank() -> loader = loader.load(GlideUrl(page.imageUrls[serverIndex], glideHeaders))
            }
            val circularProgressDrawable = CircularProgressDrawable(binding.root.context).apply {
                strokeWidth = 5f
                centerRadius = 30f
                start()
            }
            loader
                    .placeholder(circularProgressDrawable)
                    .into(object : CustomTarget<File>() {
                        override fun onLoadCleared(placeholder: Drawable?) {
                            circularProgressDrawable.start()
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            circularProgressDrawable.stop()
                            binding.chapterImage.setImage(ImageSource.resource(R.drawable.broken_image_white_192x192))
                        }

                        override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                            circularProgressDrawable.stop()
                            binding.chapterImage.setImage(ImageSource.uri(Uri.fromFile(resource)))
                        }
                    })
        }
    }
}
