package io.github.innoobwetrust.kintamanga.network

import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*


class ForceCacheInterceptor(
        private var maxAge: Long = 7776000L,
        private val expireHeader: Boolean = true
) : Interceptor {
    init {
        if (0 >= maxAge) maxAge = 7776000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val expireDate = Date().also { it.time += if (0 < maxAge) maxAge else 0 }
        val dateFormat = SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.ROOT)
                .apply { timeZone = TimeZone.getTimeZone("GMT") }
        return originalResponse.newBuilder()
                .removeHeader("Age")
                .removeHeader("Expires")
                .removeHeader("Last-Modified")
                .removeHeader("ETag")
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=$maxAge")
                .run { if (expireHeader) header("Expires", dateFormat.format(expireDate)) else this }
                .build()
    }
}