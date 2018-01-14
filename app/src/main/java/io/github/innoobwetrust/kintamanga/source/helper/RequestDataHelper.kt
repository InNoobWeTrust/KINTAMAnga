package io.github.innoobwetrust.kintamanga.source.helper

interface RequestDataHelper {
    var filterByUserInput: List<String>
    /* JSON format:
            {
                key: value,
                ...
            }
        */
    var filterBySingleChoice: Map<String, Map<String, String>>
    var filterRequiredDefaultUserInput: Map<String, String>
    /* JSON format:
        {
            key: value,
            ...
        }
    */
    var filterRequiredDefaultSingleChoice: Map<String, String>

    fun generateUserInputRequestData(
            userInput: Map<String, String>
    ): Map<String, String>? {
        val paramsMap = mutableMapOf<String, String>()
        for ((key, value) in filterRequiredDefaultUserInput) {
            paramsMap[key] = value
        }
        for ((key, value) in userInput) {
            if (!this.filterByUserInput.contains(key)) return null
            paramsMap[key] = value
        }
        return paramsMap
    }

    fun generateSingleChoiceRequestData(
            singleChoice: Map<String, String>
    ): Map<String, String>? {
        val paramsMap = mutableMapOf<String, String>()
        for ((key, value) in filterRequiredDefaultSingleChoice) {
            paramsMap[key] = value
        }
        for ((key, value) in singleChoice) {
            if (!filterBySingleChoice.containsKey(key)) return null
            if (!filterBySingleChoice[key]!!.containsValue(value)) return null
            paramsMap[key] = value
        }
        return paramsMap
    }

    fun generateRequestData(
            userInput: Map<String, String> = emptyMap(),
            singleChoice: Map<String, String> = emptyMap()
    ): Pair<Map<String, String>, Map<String, String>>? {
        val formattedUserInput = generateUserInputRequestData(userInput = userInput)
        val formattedSingleChoice = generateSingleChoiceRequestData(singleChoice = singleChoice)
        if (null == formattedUserInput ||
                null == formattedSingleChoice) return null
        return formattedUserInput to formattedSingleChoice
    }
}
