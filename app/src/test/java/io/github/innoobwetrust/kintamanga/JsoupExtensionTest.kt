package io.github.innoobwetrust.kintamanga

import io.github.innoobwetrust.kintamanga.util.extension.asJsoupDocument
import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import io.github.innoobwetrust.kintamanga.util.extension.parseString
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.junit.Test
import java.util.concurrent.TimeUnit

class JsoupExtensionTest {
    private fun loadDocument(url: String): Document {
        return OkHttpClient.Builder().build()
                .newCall(Request.Builder()
                        .url(url)
                        .headers(Headers.Builder().apply {
                            add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)")
                        }.build())
                        .cacheControl(CacheControl.Builder()
                                .maxAge(10, TimeUnit.MINUTES)
                                .build())
                        .build()
                ).execute().asJsoupDocument()
    }

    @Test
    @Throws(Exception::class)
    fun parseStringShouldBeOk() {
        val document = loadDocument("http://hocvientruyentranh.net/truyen/3927/green-tea-neko-moe-factory")
        val str = document.parseString(selector = "div.alert.alert-warning", attribute = "text")
//        val str = document.select("div.alert.alert-warning")
//        println(str.isEmpty())
        println("^$str$")
        assert(str.isNotEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun parseListStringShouldBeOk() {
        val document = loadDocument("http://hocvientruyentranh.net/truyen/3996/harapeko-no-marie")
        val listString = document.parseListString(
                selector = ".__info>p:contains(Thể loại)>a",
                attribute = "text"
        )
        println("^$listString$")
        assert(listString.isNotEmpty())
    }
}