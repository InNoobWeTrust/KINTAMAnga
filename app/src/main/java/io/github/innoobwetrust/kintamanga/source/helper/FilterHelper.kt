package io.github.innoobwetrust.kintamanga.source.helper

import io.github.innoobwetrust.kintamanga.util.extension.parseListString
import org.jsoup.nodes.Document

interface FilterHelper {
    /* JSON format:
            {
                filterKey: [
                    labelSelectorString,
                    labelAttributeString,
                    valueSelectorString,
                    valueAttributeString
                ]
                ...
            }
        */
    var dataSelectorsForSingleChoice: Map<String, List<String>>
    /* JSON format:
        {
            filterKey: [
                labelSelectorString,
                labelAttributeString,
                valueSelectorString,
                valueAttributeString
            ]
            ...
        }
    */
    var dataSelectorsForMultipleChoices: Map<String, List<String>>
    /* JSON format:
    {
        filterKey: {optionLabel: optionValue, ... }
        ...
    }
*/
    var filterBySingleChoice: Map<String, Map<String, String>>
    /* JSON format:
        {
            filterKey: {checkerLabel: checkerValue, ... }
            ...
        }
    */
    var filterByMultipleChoices: Map<String, Map<String, String>>
    var isFilterDataSingleChoiceFinalized: Boolean
    var isFilterDataMultipleChoicesFinalized: Boolean
    var isUsable: Boolean

    fun validateFilterSelector(): Boolean {
        if (!isUsable) return false
        if (isFilterDataSingleChoiceFinalized &&
                isFilterDataMultipleChoicesFinalized) {
            return true
        }
        // Check single choice
        if (dataSelectorsForSingleChoice.isNotEmpty()) {
            for ((_, value) in dataSelectorsForSingleChoice) {
                if (4 != value.size) {
                    isUsable = false
                    return false
                }
            }
        } else {
            isFilterDataSingleChoiceFinalized = true
        }
        // Check multiple choices
        if (dataSelectorsForMultipleChoices.isNotEmpty()) {
            for ((_, value) in dataSelectorsForMultipleChoices) {
                if (4 != value.size) {
                    isUsable = false
                    return false
                }
            }
        } else {
            isFilterDataMultipleChoicesFinalized = true
        }
        return true
    }

    @Throws(Exception::class)
    fun parseLabelsValuesPair(
            document: Document,
            filterSelectors: List<String>
    ): Pair<List<String>, List<String>> {
        val labelSelector = filterSelectors[0]
        val labelAttribute = filterSelectors[1]
        val valueSelector = filterSelectors[2]
        val valueAttribute = filterSelectors[3]
        val labels = document.parseListString(
                selector = labelSelector,
                attribute = labelAttribute
        )
        val values = document.parseListString(
                selector = valueSelector,
                attribute = valueAttribute
        )
        if (labels.isEmpty() || values.isEmpty() ||
                labels.size != values.size) throw Exception(
                "dataSelectors is invalid, can't parse data"
        )
        return labels to values
    }

    // Parse filter data for single choice
    fun generateSingleChoiceData(document: Document): Boolean {
        if (isFilterDataSingleChoiceFinalized) return true
        val singleValueFilterMutableMap = mutableMapOf<String, Map<String, String>>()
        for ((filterKey, filterSelectors) in dataSelectorsForSingleChoice) {
            try {
                val (labels, values) = parseLabelsValuesPair(
                        document = document,
                        filterSelectors = filterSelectors
                )
                singleValueFilterMutableMap[filterKey] = labels.mapIndexed { index, label -> label to values[index] }.toMap()
            } catch (e: Exception) {
                return false
            }
        }
        filterBySingleChoice = singleValueFilterMutableMap
        isFilterDataSingleChoiceFinalized = true
        return true
    }

    // Parse filter data for multiple choices
    fun generateMultipleChoicesData(document: Document): Boolean {
        if (isFilterDataMultipleChoicesFinalized) return true
        val multiValueFilterMutableMap = mutableMapOf<String, Map<String, String>>()
        for ((filterKey, filterSelectors) in dataSelectorsForMultipleChoices) {
            try {
                val (labels, values) = parseLabelsValuesPair(
                        document = document,
                        filterSelectors = filterSelectors
                )
                multiValueFilterMutableMap[filterKey] = labels.mapIndexed { index, label -> label to values[index] }.toMap()
            } catch (e: Exception) {
                return false
            }
        }
        filterByMultipleChoices = multiValueFilterMutableMap
        isFilterDataMultipleChoicesFinalized = true
        return true
    }
}
