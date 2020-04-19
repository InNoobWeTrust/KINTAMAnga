package io.github.innoobwetrust.kintamanga.source.helper

import io.github.innoobwetrust.kintamanga.network.GET
import io.github.innoobwetrust.kintamanga.network.POST
import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request

interface RequestHelper {
    fun headers(): Headers
    fun cacheControl(): CacheControl

    @Throws(Exception::class)
    fun buildGETRequest(url: String, forceNetwork: Boolean = false): Request = GET(
            url = url,
            headers = headers(),
            cacheControl = if (forceNetwork) CacheControl.FORCE_NETWORK else cacheControl()
    )

    @Throws(Exception::class)
    fun buildPOSTRequest(
            url: String,
            page: Int = 1,
            userInput: Map<String, String> = emptyMap(),
            singleChoice: Map<String, String> = emptyMap(),
            multipleChoices: Set<Pair<String, String>> = emptySet(),
            forceNetwork: Boolean
    ): Request {
        if (page < 1) throw IllegalArgumentException(
                "invalid page number"
        )
        val formBodyBuilder = FormBody.Builder()
        userInput.forEach { formBodyBuilder.add(it.key, it.value) }
        multipleChoices.forEach { formBodyBuilder.add(it.first, it.second) }
        singleChoice.forEach { formBodyBuilder.add(it.key, it.value) }
        formBodyBuilder.apply { if (page > 1) add("page", page.toString()) }
        return POST(
                url = url,
                requestBody = formBodyBuilder.build(),
                headers = headers(),
                cacheControl = if (forceNetwork) CacheControl.FORCE_NETWORK else cacheControl()
        )
    }
}
