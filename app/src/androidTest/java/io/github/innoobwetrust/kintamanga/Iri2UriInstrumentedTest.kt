package io.github.innoobwetrust.kintamanga

import io.github.innoobwetrust.kintamanga.util.extension.uriString
import org.junit.Test

class Iri2UriInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun iri2UriShouldBeOk() {
        val normalUri =
                "http://1.bp.blogspot.com/-1AHx4fTZFPk/WJBpo-CHNvI/AAAAAAADQx0/42Y1CmOO4vY/000.jpg?imgmax=0"
                        .uriString
        assert(normalUri.isNotBlank())
        println(normalUri)
        val normalUri2 =
                "http://hocvientruyentranh.com/manga/all?page=2".uriString
        assert(normalUri2.isNotBlank())
        println(normalUri2)
        val spaceUri =
                "http://1.bp.blogspot.com/-cffkCuGKDKQ/WJBpoUEIXBI/AAAAAAADQxw/C0M6BnCytZw/01 copy.jpg?imgmax=0"
                        .uriString
        assert(spaceUri.isNotBlank())
        println(spaceUri)
        val unicodeUri =
                "http://3.bp.blogspot.com/-mBrq6HJPM_M/WORgumVEXfI/AAAAAAAI2uI/Pe_jZ2slSOo/s0/[真臣レオン-uroco]_僧侶と交わる色欲の夜に…（1）_00001.jpg"
                        .uriString
        assert(unicodeUri.isNotBlank())
        println(unicodeUri)
        val queryUri =
                "http://www.mangahere.co/search.php?name=&author=&artist=&released=&genres[Action]=0&genres[Adventure]=0&genres[Comedy]=0&genres[Doujinshi]=0&genres[Drama]=0&genres[Ecchi]=0&genres[Fantasy]=0&genres[Gender Bender]=0&genres[Harem]=0&genres[Historical]=0&genres[Horror]=0&genres[Josei]=0&genres[Martial Arts]=0&genres[Mature]=0&genres[Mecha]=0&genres[Mystery]=0&genres[One Shot]=0&genres[Psychological]=0&genres[Romance]=0&genres[School Life]=0&genres[Sci-fi]=0&genres[Seinen]=0&genres[Shoujo]=0&genres[Shoujo Ai]=0&genres[Shounen]=0&genres[Shounen Ai]=0&genres[Slice of Life]=0&genres[Sports]=0&genres[Supernatural]=0&genres[Tragedy]=0&genres[Yaoi]=0&genres[Yuri]=0&name_method=cw&author_method=cw&artist_method=cw&released_method=eq&direction=&is_completed=&advopts=1&page=2"
                        .uriString
        assert(queryUri.isNotBlank())
        println(queryUri)
        val corrrectRawUri =
                "http://images2-opensocial.googleusercontent.com/gadgets/proxy?container=focus&gadget=a&resize_h=225&resize_w=170&no_expand=0&refresh=31536000&rewriteMime=image%2F*&url=http%3A%2F%2Fi.imgur.com%2Fh0CHDCr.jpg"
        val correctUri = corrrectRawUri.uriString
        assert(corrrectRawUri == correctUri)
        println(correctUri)
    }
}