package io.github.innoobwetrust.kintamanga.util

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import io.github.innoobwetrust.kintamanga.util.Storage.glideCache

@GlideModule
class OkHttpExternalCacheGlideModule : AppGlideModule(), KodeinGlobalAware {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(
                context,
                glideCache.first,
                glideCache.second
        ))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        glide?.registry?.replace(
//                GlideUrl::class.java,
//                InputStream::class.java,
//                OkHttpUrlLoader.Factory(instance<OkHttpClient>("cover"))
//        )
    }
}