package com.nova.plugin.main


object PatternUtils {

    fun convertToPatternString(input: String): String {
        val searchList = arrayOf(".", "?", "*", "+")
        val replacementList = arrayOf("\\.", ".?", ".*", ".+")
        return replaceEach(input, searchList, replacementList)
    }

    private fun replaceEach(text: String, searchList: Array<String>, replacementList: Array<String>): String {
        val tracker = SearchTracker(text, searchList, replacementList)
        if (!tracker.hasNextMatch(0)) {
            return text
        }
        val buf = StringBuilder(text.length * 2)
        var start = 0
        do {
            val matchInfo = tracker.matchInfo
            val textIndex = matchInfo!!.textIndex
            val pattern = matchInfo.pattern
            val replacement = matchInfo.replacement
            buf.append(text.substring(start, textIndex))
            buf.append(replacement)
            start = textIndex + pattern.length
        } while (tracker.hasNextMatch(start))
        return buf.append(text.substring(start)).toString()
    }


}
data class SearchTracker (val text: String, val searchList: Array<String>, val replacementList: Array<String>) {

    private val patternToReplacement: MutableMap<String?, String?> = HashMap()
    private val pendingPatterns: MutableSet<String?> = HashSet()
    internal var matchInfo: MatchInfo? = null

    init {
        for (i in searchList.indices) {
            val pattern = searchList[i]
            patternToReplacement[pattern] = replacementList[i]
            pendingPatterns.add(pattern)
        }
    }

    fun hasNextMatch(start: Int): Boolean {
        var textIndex = -1
        var nextPattern: String? = null
        val it  = pendingPatterns.iterator()
        while (it.hasNext()) {
            val pattern = it.next() as String
            val matchIndex = text.indexOf(pattern, start)
            if (matchIndex == -1) {
                pendingPatterns.remove(pattern)
            } else if (textIndex == -1 || matchIndex < textIndex) {
                textIndex = matchIndex
                nextPattern = pattern
            }
        }
        if (nextPattern != null) {
            matchInfo = MatchInfo(nextPattern, patternToReplacement[nextPattern], textIndex)
            return true
        }
        return false
    }

    class MatchInfo internal constructor(val pattern: String, val replacement: String?, val textIndex: Int)
}