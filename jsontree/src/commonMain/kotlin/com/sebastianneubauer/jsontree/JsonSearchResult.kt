package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable
import com.sebastianneubauer.jsontree.JsonTreeElement.ParentType
import com.sebastianneubauer.jsontree.JsonTreeElement.Primitive

public data class JsonSearchResult(
    val searchKeyValue: String? = null,
    val jsonQuery: String? = null,
    val totalListSize: Int? = null,
    val highlightedLines: List<Int> = emptyList(),
    val currentHighlightedLine: Int = -1,
)

@Composable
public fun rememberJsonSearchResultState(
    initSearchKeyValue: String? = null,
    initTotalListSize: Int? = null,
    initialHighlightedLines: List<Int> = emptyList(),
    initialCurrentHighlightedLine: Int = -1
): JsonSearchResultState {
    return remember {
        JsonSearchResultState(
            initSearchKeyValue = initSearchKeyValue,
            initTotalListSize = initTotalListSize,
            initialHighlightedLines = initialHighlightedLines,
            initialCurrentHighlightedLine = initialCurrentHighlightedLine
        )
    }
}

public class JsonSearchResultState(
    initSearchKeyValue: String? = null,
    initJsonQuery: String? = null,
    initTotalListSize: Int? = null,
    initialHighlightedLines: List<Int> = emptyList(),
    initialCurrentHighlightedLine: Int = -1
) {
    // Internal state representing the current search result
    public var state: JsonSearchResult by mutableStateOf(
        JsonSearchResult(
            searchKeyValue = initSearchKeyValue,
            jsonQuery = initJsonQuery,
            totalListSize = initTotalListSize,
            highlightedLines = initialHighlightedLines,
            currentHighlightedLine = initialCurrentHighlightedLine
        )
    )

    public fun matchFound(): Boolean {
        return totalFound() > 0
    }

    public fun totalFound(): Int {
        return state.highlightedLines.size
    }

    public fun currentFound(): Int {
        return state.currentHighlightedLine + 1
    }

    // Move to the next highlighted line
    public fun next() {
        val nextValue = if (state.currentHighlightedLine < state.highlightedLines.size - 1) {
            state.currentHighlightedLine + 1
        } else {
            // loop to first match
            0
        }

        state = state.copy(currentHighlightedLine = nextValue)
    }

    // Move to the previous highlighted line
    public fun previous() {
        val nextValue = if (state.currentHighlightedLine > 0) {
            state.currentHighlightedLine - 1
        } else {
            // loop to last match
            state.highlightedLines.size - 1
        }
        state = state.copy(currentHighlightedLine = nextValue)
    }
}

internal fun JsonTreeElement.getJsonQuery(
    parts: List<String>?,
    adjacentMap: MutableMap<Int, Int>?
): String? {
    if (parts.isNullOrEmpty() || adjacentMap == null) return null
    var lvl = level - 1

    when {
        this is Primitive && parentType == ParentType.ARRAY -> lvl--
        this is Collapsable.Object && parentType == ParentType.ARRAY -> lvl--
        this is Collapsable.Array && parentType == ParentType.ARRAY -> lvl--
    }

    lvl -= (adjacentMap[lvl] ?: 0)

    return parts.getOrNull(lvl)
}

internal fun String?.getAdjacentMap(): Pair<List<String>?, MutableMap<Int, Int>?> {
    if (this == null) return null to null
    val parts = split(".")
    val map = mutableMapOf<Int, Int>()
    var nestedArrayCount = 0
    parts.forEachIndexed { i, p ->
        map[i + nestedArrayCount] = nestedArrayCount
        if (p.contains("[") && p.endsWith("]")) {
            nestedArrayCount++
        }
    }
    return parts to map
}

internal fun splitArrayNotation(
    jsonQuery: String?
): Pair<String?, String?> {
    if (jsonQuery == null ||
        !jsonQuery.contains("[") && !jsonQuery.endsWith("]")
    ) {
        return Pair(jsonQuery, null)
    }

    val parts = jsonQuery.split("[")
    val key = parts.getOrNull(0)
    val indexString = parts.getOrNull(1)?.split("]")?.getOrNull(0)

    return Pair(key, indexString)
}

internal fun searchText(
    text: String,
    searchKey: String?,
    wordMatch: Boolean = false
): Triple<String, String?, String?> {
    var res: Triple<String, String?, String?> = Triple(text, null, null)
    if (!searchKey.isNullOrEmpty()) {
        if (wordMatch) {
            if (text.equals(searchKey, ignoreCase = true)) {
                res = Triple("", text, "")
            }
        } else {
            val regex = "(?i)${Regex.escape(searchKey)}".toRegex()
            val match = regex.find(text)
            if (match != null) {
                val before = text.substring(0, match.range.first)
                val foundText = match.value // The actual text found in the original string
                val after = text.substring(match.range.last + 1)
                res = Triple(before, foundText, after)
                return res
            }
        }
    }
    return res
}

internal fun JsonTreeElement.keyMatch(jsonQuery: String?): Boolean {
    if (key == null || jsonQuery == null) return false
    val match = key.equals(jsonQuery, true) || key.equals(jsonQuery, true)
    return this is Collapsable && state == TreeState.COLLAPSED && (match)
}
