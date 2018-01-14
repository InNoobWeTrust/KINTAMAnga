package io.github.innoobwetrust.kintamanga

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.google.gson.Gson
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.network.ForceCacheInterceptor
import io.github.innoobwetrust.kintamanga.util.Storage
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

fun appModule(app: Application) = Kodein.Module {
    //    constant("density") with app.resources.displayMetrics.density
    bind<Gson>() with singleton { Gson() }
    bind<DatabaseHelper>() with instance(DatabaseHelper(app))
    bind<Resources>() with instance(app.resources)
    bind<File>("cache") with instance(app.cacheDir)
    bind<File>("externalCache") with instance(app.externalCacheDir ?: app.cacheDir)
    bind<File>("files") with instance(app.filesDir)
    bind<File>("externalFiles") with instance(app.getExternalFilesDir(null) ?: app.filesDir)
    bind<Downloader>() with instance(Downloader(app))
    bind<ClearableCookieJar>() with instance(
            PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(app))
    )
}

fun serviceModule(app: Application) = Kodein.Module {
//    bind<NotificationCompat.Builder>() with instance(
//            NotificationCompat.Builder(app, app.getString(R.string.app_name))
//    )
    bind<ConnectivityManager>() with instance(
            app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    )
//    bind<NotificationManager>() with instance(
//            app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    )
}

fun preferencesModule(app: Application) = Kodein.Module {
    bind<SharedPreferences>(KINTAMAngaPreferences.MAIN_ACTIVITY.key) with instance(
            app.getSharedPreferences(
                    KINTAMAngaPreferences.MAIN_ACTIVITY.key,
                    Context.MODE_PRIVATE
            )
    )
    bind<SharedPreferences>(KINTAMAngaPreferences.ACTIVE_DOWNLOADS.key) with instance(
            app.getSharedPreferences(
                    KINTAMAngaPreferences.ACTIVE_DOWNLOADS.key,
                    Context.MODE_PRIVATE
            )
    )
}

val okHttpModule = Kodein.Module {
    bind<Headers>() with singleton {
        Headers.Builder().apply {
            add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
        }.build()
    }
    bind<CacheControl>() with singleton {
        CacheControl.Builder()
                .maxAge(5, TimeUnit.MINUTES)
                .build()
    }
    bind<OkHttpClient>("default") with singleton {
        OkHttpClient.Builder().cookieJar(instance<ClearableCookieJar>()).build()
    }
    bind<OkHttpClient>() with singleton {
        instance<OkHttpClient>("default")
                .newBuilder()
                .cache(Cache(Storage.htmlCacheDir, Storage.htmlCacheSize))
                .addNetworkInterceptor(ForceCacheInterceptor(
                        maxAge = 60L,
                        expireHeader = false
                ))
                .addInterceptor { chain ->
                    chain.proceed(chain.request().run {
                        if (instance<ConnectivityManager>()
                                .activeNetworkInfo
                                ?.isConnected == true) {
                            this
                        } else {
                            newBuilder().header(
                                    "Cache-Control",
                                    "public, only-if-cached, max-stale=2419200"
                            ).build()
                        }
                    })
                }
                .build()
    }
    bind<OkHttpClient>("cover") with singleton {
        instance<OkHttpClient>("default")
                .newBuilder()
                .cache(Cache(Storage.coverCacheDir, Storage.coverCacheSize))
                .addNetworkInterceptor(ForceCacheInterceptor())
                .build()
    }
    bind<OkHttpClient>("chapter") with singleton {
        instance<OkHttpClient>("default")
                .newBuilder()
                .cache(Cache(Storage.chapterCacheDir, Storage.chapterCacheSize))
                .addNetworkInterceptor(ForceCacheInterceptor())
                .build()
    }
}
