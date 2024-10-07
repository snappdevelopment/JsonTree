package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

public data class JsonSearchResult(
    val searchKeyValue: String? = null,
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
    initTotalListSize: Int? = null,
    initialHighlightedLines: List<Int> = emptyList(),
    initialCurrentHighlightedLine: Int = -1
) {
    // Internal state representing the current search result
    public var state: JsonSearchResult by mutableStateOf(
        JsonSearchResult(
            searchKeyValue = initSearchKeyValue,
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
