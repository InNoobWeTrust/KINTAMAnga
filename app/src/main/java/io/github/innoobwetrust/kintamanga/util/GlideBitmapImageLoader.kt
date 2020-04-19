package io.github.innoobwetrust.kintamanga.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crashlytics.android.Crashlytics
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import io.github.innoobwetrust.kintamanga.R
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class GlideBitmapImageLoader private constructor(
        context: Context,
        okHttpClient: OkHttpClient?
) : ImageLoader {
    private val mRequestManager: RequestManager
    private val prefetchTargets: MutableList<Pair<Uri,CustomTarget<File>>> = mutableListOf()

    init {
        GlideProgressSupport.init(Glide.get(context), okHttpClient)
        mRequestManager = Glide.with(context)
    }

    override fun loadImage(uri: Uri, callback: ImageLoader.Callback) {
        mRequestManager
                .downloadOnly()
                .load(uri)
                .into(object : ImageDownloadTarget(uri.toString()) {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        resource.let {
                            Observable
                                    .fromCallable {
                                        ImageConverter.convertToSupportedImage(source = it)
                                    }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            { success ->
                                                if (success) {
                                                    callback.onCacheHit(it)
                                                    callback.onSuccess(it)
                                                } else {
                                                    callback.onFail(Exception("Failed to convert image to bitmap"))
                                                }
                                            },
                                            { error ->
                                                Timber.e(error)
                                                Crashlytics.logException(error)
                                                callback.onFail(Exception("Failed to convert image to bitmap"))
                                            }
                                    )
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        callback.onFail(Exception("Failed to load image"))
                    }

                    override fun onDownloadStart() {
                        callback.onStart()
                    }

                    override fun onProgress(progress: Int) {
                        callback.onProgress(progress)
                    }

                    override fun onDownloadFinish() {
                        callback.onFinish()
                    }
                })
    }

    override fun showThumbnail(parent: BigImageView, thumbnail: Uri, scaleType: Int): View {
        val thumbnailView = LayoutInflater.from(parent.context)
                .inflate(R.layout.ui_glide_thumbnail, parent, false) as ImageView
        when (scaleType) {
            BigImageView.INIT_SCALE_TYPE_CENTER_CROP ->
                thumbnailView.scaleType = ImageView.ScaleType.CENTER_CROP
            BigImageView.INIT_SCALE_TYPE_CENTER_INSIDE ->
                thumbnailView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        mRequestManager
                .load(thumbnail)
                .into(thumbnailView)
        return thumbnailView
    }

    override fun prefetch(uri: Uri) {
        if(prefetchTargets.none { it.first == uri }) {
            mRequestManager
                    .downloadOnly()
                    .load(uri)
                    .into(object : CustomTarget<File>() {
                        override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                            ImageConverter.convertToSupportedImage(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    }.also {
                        if (prefetchTargets.size > 7)
                            mRequestManager.clear(prefetchTargets.removeAt(0).second)
                        prefetchTargets.add(uri to it)
                    })
        }
    }

    fun cancelPrefetch() {
        prefetchTargets.forEach {
            mRequestManager.clear(it.second)
        }
        prefetchTargets.clear()
    }

    companion object {
        @JvmOverloads
        fun with(
                context: Context,
                okHttpClient: OkHttpClient? = null
        ): GlideBitmapImageLoader {
            return GlideBitmapImageLoader(context, okHttpClient)
        }
    }
}

object GlideProgressSupport {
    private fun createInterceptor(listener: ResponseProgressListener): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            response.newBuilder()
                    .body(OkHttpProgressResponseBody(
                            request.url,
                            response.body ?: "".toResponseBody(null),
                            listener
                    ))
                    .build()
        }
    }

    fun init(glide: Glide, okHttpClient: OkHttpClient?) {
        val builder: OkHttpClient.Builder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()
        builder.addNetworkInterceptor(createInterceptor(DispatchingProgressListener()))
        glide.registry.replace(
                GlideUrl::class.java,
                InputStream::class.java,
                OkHttpUrlLoader.Factory(builder.build())
        )
    }

    fun forget(url: String) {
        DispatchingProgressListener.forget(url)
    }

    fun expect(url: String, listener: ProgressListener) {
        DispatchingProgressListener.expect(url, listener)
    }

    interface ProgressListener {
        fun onDownloadStart()

        fun onProgress(progress: Int)

        fun onDownloadFinish()
    }

    private interface ResponseProgressListener {
        fun update(url: HttpUrl, bytesRead: Long, contentLength: Long)
    }

    private class DispatchingProgressListener : ResponseProgressListener {

        override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {
            val key = url.toString()
            val listener = LISTENERS[key] ?: return

            val lastProgress = PROGRESSES[key]
            if (lastProgress == null) {
                // ensure `onStart` is called before `onProgress` and `onFinish`
                listener.onDownloadStart()
            }
            if (contentLength <= bytesRead) {
                listener.onDownloadFinish()
                forget(key)
                return
            }
            val progress = (bytesRead.toFloat() / contentLength * 100).toInt()
            if (lastProgress == null || progress != lastProgress) {
                PROGRESSES[key] = progress
                listener.onProgress(progress)
            }
        }

        companion object {
            private val LISTENERS = HashMap<String, ProgressListener>()
            private val PROGRESSES = HashMap<String, Int>()

            internal fun forget(url: String) {
                LISTENERS.remove(url)
                PROGRESSES.remove(url)
            }

            internal fun expect(url: String, listener: ProgressListener) {
                LISTENERS[url] = listener
            }
        }
    }

    private class OkHttpProgressResponseBody internal constructor(
            private val mUrl: HttpUrl,
            private val mResponseBody: ResponseBody,
            private val mProgressListener: ResponseProgressListener
    ) : ResponseBody() {
        private var mBufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return mResponseBody.contentType()
        }

        override fun contentLength(): Long {
            return mResponseBody.contentLength()
        }

        override fun source(): BufferedSource {
            return mBufferedSource
                    ?: source(mResponseBody.source()).buffer()
                    .also { mBufferedSource = it }
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                private var mTotalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    val fullLength = mResponseBody.contentLength()
                    if (bytesRead == -1L) { // this source is exhausted
                        mTotalBytesRead = fullLength
                    } else {
                        mTotalBytesRead += bytesRead
                    }
                    mProgressListener.update(mUrl, mTotalBytesRead, fullLength)
                    return bytesRead
                }
            }
        }
    }
}

abstract class ImageDownloadTarget protected constructor(private val mUrl: String) :
        CustomTarget<File>(),
        GlideProgressSupport.ProgressListener {

    override fun onLoadCleared(placeholder: Drawable?) {
        GlideProgressSupport.forget(mUrl)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        super.onLoadStarted(placeholder)
        GlideProgressSupport.expect(mUrl, this)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        GlideProgressSupport.forget(mUrl)
    }
}
