package com.sebastianneubauer.jsontree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal data class SearchResult(
    val searchQuery: String?,
    internal val searchOccurrences: Map<Int, SearchOccurrence>,
    internal val selectedSearchOccurrence: SelectedSearchOccurrence?,
    internal val resultCount: Int,
    internal val selectedResultIndex: Int,
)

@Composable
public fun rememberSearchState(
    initialSearchQuery: String? = null,
): SearchState {
    return remember {
        SearchState(initialSearchQuery = initialSearchQuery)
    }
}

public class SearchState(
    initialSearchQuery: String? = null,
) {
    // Internal state representing the current search result
    internal var state: SearchResult by mutableStateOf(
        SearchResult(
            searchQuery = initialSearchQuery,
            searchOccurrences = emptyMap(),
            selectedSearchOccurrence = null,
            resultCount = 0,
            selectedResultIndex = -1
        )
    )

    public var searchQuery: String?
        set(value) { state = state.copy(searchQuery = value) }
        get() = state.searchQuery

    public val resultCount: Int
        get() = state.resultCount

    public val selectedResult: Int
        get() = state.selectedResultIndex + 1

    // Move to the next highlighted line
    public fun selectNext() {
        val occurrences = state.searchOccurrences
        val selectedOccurrence = state.selectedSearchOccurrence

        if (occurrences.isEmpty()) return

        val updatedSelectedOccurrence = when {
            selectedOccurrence == null -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.getValue(0),
                    rangeIndex = 0
                )
            }
            selectedOccurrence.rangeIndex != selectedOccurrence.occurrence.ranges.lastIndex -> {
                selectedOccurrence.copy(rangeIndex = selectedOccurrence.rangeIndex + 1)
            }

            selectedOccurrence.occurrence.listIndex != occurrences.keys.last() -> {
                val selectedOccurrenceIndex = occurrences.keys.indexOf(selectedOccurrence.occurrence.listIndex)
                val nextOccurrence = occurrences.values.elementAt(selectedOccurrenceIndex + 1)
                SelectedSearchOccurrence(
                    occurrence = nextOccurrence,
                    rangeIndex = 0
                )
            }
            else -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.values.first(),
                    rangeIndex = 0
                )
            }
        }

//        val selectedResultIndex = occurrences.entries
//            .takeWhile { it.value != updatedSelectedOccurrence.occurrence }
//            .sumOf { it.value.rangeCount }
//            .plus(updatedSelectedOccurrence.rangeIndex) // TODO: check if its off by one

        val maxSelectedResultIndex = occurrences.values.sumOf { it.rangeCount } - 1 // TODO: check if its off by one
        val selectedResultIndex = if (state.selectedResultIndex == maxSelectedResultIndex) {
            0
        } else {
            maxSelectedResultIndex
        }

        state = state.copy(
            selectedSearchOccurrence = updatedSelectedOccurrence,
            selectedResultIndex = selectedResultIndex
        )
    }

    // Move to the previous highlighted line
    public fun selectPrevious() {
        val occurrences = state.searchOccurrences
        val selectedOccurrence = state.selectedSearchOccurrence

        if (occurrences.isEmpty()) return

        val updatedSelectedOccurrence = when {
            selectedOccurrence == null -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.entries.last().value,
                    rangeIndex = occurrences.entries.last().value.ranges.lastIndex
                )
            }
            selectedOccurrence.rangeIndex != 0 -> {
                selectedOccurrence.copy(rangeIndex = selectedOccurrence.rangeIndex - 1)
            }
            selectedOccurrence.occurrence.listIndex != occurrences.keys.first() -> {
                val selectedOccurrenceIndex = occurrences.keys.indexOf(selectedOccurrence.occurrence.listIndex)
                val previousOccurrence = occurrences.values.elementAt(selectedOccurrenceIndex - 1)
                SelectedSearchOccurrence(
                    occurrence = previousOccurrence,
                    rangeIndex = previousOccurrence.ranges.lastIndex
                )
            }
            else -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.values.last(),
                    rangeIndex = occurrences.values.last().ranges.lastIndex
                )
            }
        }

//        val selectedResultIndex = occurrences
//            .takeWhile { it != updatedSelectedOccurrence.occurrence }
//            .sumOf { it.rangeCount }
//            .plus(updatedSelectedOccurrence.rangeIndex) // TODO: check if its off by one

        val selectedResultIndex = if (state.selectedResultIndex == -1) {
            occurrences.values.sumOf { it.rangeCount } - 1 // TODO: check if its off by one
        } else {
            0
        }

        state = state.copy(
            selectedSearchOccurrence = updatedSelectedOccurrence,
            selectedResultIndex = selectedResultIndex
        )
    }

    internal fun reset() {
        state = SearchResult(
            searchQuery = null,
            searchOccurrences = emptyMap(),
            selectedSearchOccurrence = null,
            selectedResultIndex = -1,
            resultCount = 0
        )
    }
}
