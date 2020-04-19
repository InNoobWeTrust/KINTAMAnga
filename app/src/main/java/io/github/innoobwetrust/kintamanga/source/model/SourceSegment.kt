package io.github.innoobwetrust.kintamanga.source.model

import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.Response
import java.io.Serializable

interface SourceSegment : Serializable {
    /**
     * Use this to get reference to processors in this source
     */
    var source: Source
    /**
     * Use this to display the name of this segment in UI
     */
    var pathName: String
    /**
     * Use this to generate URL for this segment
     */
    var pathSegment: String
    /**
     * Use this to display a pretty version of filter keys in UI
     */
    var filterKeyLabel: Map<String, String>
    /**
     * Use this to get the list of available keys for user input
     */
    var filterByUserInput: List<String>
    /**
     * Use this to get the list of available keys and their options (label-value pair) for single choice
     */
    var filterBySingleChoice: Map<String, Map<String, String>>
    /**
     * Use this to get the list of available keys and their options (label-value pair) for multiple choices
     */
    var filterByMultipleChoices: Map<String, Map<String, String>>
    /**
     * Use this to get the list of required keys and their default values for user input
     */
    var filterRequiredDefaultUserInput: Map<String, String>
    /**
     * Use this to get the list of required keys and their default values for single choice
     */
    var filterRequiredDefaultSingleChoice: Map<String, String>
    /**
     * Header for segments which require special header to access
     */
    fun headers(): Headers

    /**
     * Can custom the cache policy for individual segment
     */
    fun cacheControl(): CacheControl

    /**
     * Use this to fetch filter data before showing and doing request
     */
    fun fetchFilterData(): Boolean

    /**
     * Use this to fetch segment data based on filter
     */
    @Throws(Exception::class)
    fun fetchData(
            pageNumber: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>,
            forceRefresh: Boolean = false
    ): Response

    /**
     * Use this to fetch segment data with given link (eg: link to next page)
     */
    @Throws(Exception::class)
    fun fetchData(uri: String): Response

    /**
     * Use this to process the response into [CatalogPage]
     */
    fun catalogPageFromResponse(response: Response, pageNumber: Int): CatalogPage

    /**
     * Fetch data and process the response into [CatalogPage] then return
     */
    @Throws(Exception::class)
    fun fetchCatalogPage(
            pageNumber: Int,
            userInput: Map<String, String>,
            singleChoice: Map<String, String>,
            multipleChoices: Set<Pair<String, String>>,
            forceRefresh: Boolean = false
    ): CatalogPage = catalogPageFromResponse(
            response = fetchData(
                    pageNumber = pageNumber,
                    userInput = userInput,
                    singleChoice = singleChoice,
                    multipleChoices = multipleChoices,
                    forceRefresh = forceRefresh
            ),
            pageNumber = pageNumber
    )
}