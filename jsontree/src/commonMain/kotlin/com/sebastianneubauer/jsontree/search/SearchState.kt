package com.sebastianneubauer.jsontree.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Remembers an instance of [SearchState].
 * Use this to get current search data for a given [SearchState.query].
 */
@Composable
public fun rememberSearchState(): SearchState {
    return remember {
        SearchState(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main,
        )
    }
}

/**
 * Represents the current search data for a given [query].
 */
public class SearchState internal constructor(
    private val defaultDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
) {
    internal var state: SearchResult by mutableStateOf(
        SearchResult(
            query = null,
            occurrences = emptyMap(),
            selectedOccurrence = null,
            totalResults = 0,
            selectedResultIndex = null
        )
    )

    /**
     * The search term for which produced the current search results.
     */
    public var query: String?
        set(value) { state = state.copy(query = value) }
        get() = state.query

    /**
     * The total amount of results found for the [query].
     */
    public val totalResults: Int
        get() = state.totalResults

    /**
     * The index of the currently selected result.
     * A number between 0 and `totalResults - 1` if there are any results for the current query.
     * Null in case there are no results.
     * Use [selectNext] and [selectPrevious] to select a different result.
     */
    public val selectedResultIndex: Int?
        get() = state.selectedResultIndex

    /**
     * The index of the list item in which the selected result is located, or null if there are
     * no results.
     */
    public val selectedResultListIndex: Int?
        get() = state.selectedOccurrence?.occurrence?.listIndex

    /**
     * Select the next result.
     * If the currently selected result is the last one, the first result will be selected.
     */
    public suspend fun selectNext(): Unit = withContext(defaultDispatcher) {
        val occurrences = state.occurrences
        val selectedOccurrence = state.selectedOccurrence
        val selectedResultIndex = state.selectedResultIndex

        if (occurrences.isEmpty() || selectedOccurrence == null || selectedResultIndex == null) {
            return@withContext
        }

        val updatedSelectedOccurrence = when {
            selectedOccurrence.range != selectedOccurrence.occurrence.ranges.last() -> {
                val rangeIndex = selectedOccurrence.occurrence.ranges.indexOf(selectedOccurrence.range)
                selectedOccurrence.copy(range = selectedOccurrence.occurrence.ranges[rangeIndex + 1])
            }

            selectedOccurrence.occurrence.listIndex != occurrences.keys.last() -> {
                val selectedOccurrenceIndex = occurrences.keys.indexOf(selectedOccurrence.occurrence.listIndex)
                val nextOccurrence = occurrences.values.elementAt(selectedOccurrenceIndex + 1)
                SelectedSearchOccurrence(
                    occurrence = nextOccurrence,
                    range = nextOccurrence.ranges.first()
                )
            }
            else -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.values.first(),
                    range = occurrences.values.first().ranges.first()
                )
            }
        }

        val maxSelectedResultIndex = occurrences.values.sumOf { it.ranges.size } - 1
        val updatedSelectedResultIndex = if (selectedResultIndex == maxSelectedResultIndex) {
            0
        } else {
            selectedResultIndex + 1
        }

        withContext(mainDispatcher) {
            state = state.copy(
                selectedOccurrence = updatedSelectedOccurrence,
                selectedResultIndex = updatedSelectedResultIndex
            )
        }
    }

    /**
     * Select the previous result.
     * If the currently selected result is the first one, the last result will be selected.
     */
    public suspend fun selectPrevious(): Unit = withContext(defaultDispatcher) {
        val occurrences = state.occurrences
        val selectedOccurrence = state.selectedOccurrence
        val selectedResultIndex = state.selectedResultIndex

        if (occurrences.isEmpty() || selectedOccurrence == null || selectedResultIndex == null) {
            return@withContext
        }

        val updatedSelectedOccurrence = when {
            selectedOccurrence.range != selectedOccurrence.occurrence.ranges.first() -> {
                val rangeIndex = selectedOccurrence.occurrence.ranges.indexOf(selectedOccurrence.range)
                selectedOccurrence.copy(range = selectedOccurrence.occurrence.ranges[rangeIndex - 1])
            }
            selectedOccurrence.occurrence.listIndex != occurrences.keys.first() -> {
                val selectedOccurrenceIndex = occurrences.keys.indexOf(selectedOccurrence.occurrence.listIndex)
                val previousOccurrence = occurrences.values.elementAt(selectedOccurrenceIndex - 1)
                SelectedSearchOccurrence(
                    occurrence = previousOccurrence,
                    range = previousOccurrence.ranges.last()
                )
            }
            else -> {
                SelectedSearchOccurrence(
                    occurrence = occurrences.values.last(),
                    range = occurrences.values.last().ranges.last()
                )
            }
        }

        val maxSelectedResultIndex = occurrences.values.sumOf { it.ranges.size } - 1
        val updatedSelectedResultIndex = if (selectedResultIndex == 0) {
            maxSelectedResultIndex
        } else {
            selectedResultIndex - 1
        }

        withContext(mainDispatcher) {
            state = state.copy(
                selectedOccurrence = updatedSelectedOccurrence,
                selectedResultIndex = updatedSelectedResultIndex
            )
        }
    }

    internal fun reset() {
        state = SearchResult(
            query = null,
            occurrences = emptyMap(),
            selectedOccurrence = null,
            totalResults = 0,
            selectedResultIndex = null
        )
    }

    @Immutable
    internal data class SearchResult(
        val query: String?,
        val occurrences: Map<Int, SearchOccurrence>,
        val selectedOccurrence: SelectedSearchOccurrence?,
        val totalResults: Int,
        val selectedResultIndex: Int?,
    )
}
