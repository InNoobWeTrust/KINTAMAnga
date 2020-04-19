package io.github.innoobwetrust.kintamanga.util

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.github.innoobwetrust.kintamanga.R

object BindingAdapter {
    @JvmStatic
    @BindingAdapter("imageUriCenterCrop")
    fun loadImageCenterCrop(view: ImageView, imageUri: String?) {
        if (imageUri.isNullOrBlank()) return
        Glide.with(view.context)
                .load(imageUri)
                .apply(
                        RequestOptions.centerCropTransform()
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(true)
                                .error(R.drawable.broken_image_white_192x192)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }

    @JvmStatic
    @BindingAdapter("imageUriFitCenter")
    fun loadImageFitCenter(view: ImageView, imageUri: String?) {
        if (imageUri.isNullOrBlank()) return
        Glide.with(view.context)
                .load(imageUri)
                .apply(
                        RequestOptions.fitCenterTransform()
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .skipMemoryCache(true)
                                .error(R.drawable.broken_image_white_192x192)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }

    @JvmStatic
    @BindingAdapter("srcCompat")
    fun setImageResource(imageView: ImageView, @DrawableRes resource: Int?) {
        if (null != resource) imageView.setImageResource(resource)
    }
}
