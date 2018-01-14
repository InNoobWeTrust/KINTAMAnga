package io.github.innoobwetrust.kintamanga.network

import okhttp3.*
import timber.log.Timber
import java.util.concurrent.TimeUnit.MINUTES

private val DEFAULT_HEADERS: Headers by lazy {
    Headers.Builder().build()
}
private val DEFAULT_CACHE_CONTROL: CacheControl by lazy {
    CacheControl.Builder().maxAge(10, MINUTES).build()
}
private val DEFAULT_FORM: RequestBody by lazy {
    FormBody.Builder().build()
}

fun GET(url: String,
        headers: Headers? = DEFAULT_HEADERS,
        cacheControl: CacheControl? = DEFAULT_CACHE_CONTROL
): Request {
    return Request.Builder()
            .url(url)
            .headers(headers ?: DEFAULT_HEADERS)
            .cacheControl(cacheControl ?: DEFAULT_CACHE_CONTROL)
            .get()
            .build()
            .also {
                Timber.d("Headers:\n%s", it.headers)
            }
}

fun POST(
        url: String,
        requestBody: RequestBody? = DEFAULT_FORM,
        headers: Headers? = DEFAULT_HEADERS,
        cacheControl: CacheControl? = DEFAULT_CACHE_CONTROL
): Request {
    return Request.Builder()
            .url(url)
            .post(requestBody ?: DEFAULT_FORM)
            .headers(headers ?: DEFAULT_HEADERS)
            .cacheControl(cacheControl ?: DEFAULT_CACHE_CONTROL)
            .build()
}
