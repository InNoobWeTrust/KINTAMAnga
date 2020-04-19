package io.github.innoobwetrust.kintamanga.source.processor

import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.source.model.Source
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.Serializable

interface BaseInfoProcessor : Serializable, KodeinGlobalAware {
    var source: Source

    fun headers(): Headers
    fun cacheControl(): CacheControl

    @Throws(Exception::class)
    fun fetchData(
            uri: String,
            headers: Headers = headers(),
            cacheControl: CacheControl = cacheControl()
    ): Response {
        val request = GET(
                url = uri,
                headers = headers,
                cacheControl = cacheControl
        )
        return instance<OkHttpClient>().newCall(request).execute()
    }
}