package com.sebastianneubauer.jsontree.search

import androidx.compose.runtime.Composable
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
public fun rememberSearchState(
    initialSearchQuery: String? = null,
): SearchState {
    return remember {
        SearchState(
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main,
            initialSearchQuery = initialSearchQuery
        )
    }
}

/**
 * Represents the current search data for a given [query].
 */
public class SearchState internal constructor(
    private val defaultDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
    private val initialSearchQuery: String? = null,
) {
    internal var state: SearchResult by mutableStateOf(
        SearchResult(
            searchQuery = initialSearchQuery,
            searchOccurrences = emptyMap(),
            selectedSearchOccurrence = null,
            resultCount = 0,
            selectedResultIndex = -1
        )
    )

    /**
     * The search term for which produced the current search results.
     */
    public var query: String?
        set(value) { state = state.copy(searchQuery = value) }
        get() = state.searchQuery

    /**
     * The total amount of results found for the [query].
     */
    public val totalResults: Int
        get() = state.resultCount

    /**
     * The currently selected result. A number between 1 and [totalResults].
     * Use [selectNext] and [selectPrevious] to select a different result.
     */
    public val selectedResult: Int
        get() = state.selectedResultIndex + 1

    /**
     * Select the next result.
     * If the currently selected result is the last one, the first result will be selected.
     */
    public suspend fun selectNext(): Unit = withContext(defaultDispatcher) {
        val occurrences = state.searchOccurrences
        val selectedOccurrence = state.selectedSearchOccurrence

        if (occurrences.isEmpty() || selectedOccurrence == null) return@withContext

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

        val maxSelectedResultIndex = occurrences.values.sumOf { it.rangeCount } - 1
        val selectedResultIndex = if (state.selectedResultIndex == maxSelectedResultIndex) {
            0
        } else {
            state.selectedResultIndex + 1
        }

        withContext(mainDispatcher) {
            state = state.copy(
                selectedSearchOccurrence = updatedSelectedOccurrence,
                selectedResultIndex = selectedResultIndex
            )
        }
    }

    /**
     * Select the previous result.
     * If the currently selected result is the first one, the last result will be selected.
     */
    public suspend fun selectPrevious(): Unit = withContext(defaultDispatcher) {
        val occurrences = state.searchOccurrences
        val selectedOccurrence = state.selectedSearchOccurrence

        if (occurrences.isEmpty() || selectedOccurrence == null) return@withContext

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

        val maxSelectedResultIndex = occurrences.values.sumOf { it.rangeCount } - 1
        val selectedResultIndex = if (state.selectedResultIndex == 0) {
            maxSelectedResultIndex
        } else {
            state.selectedResultIndex - 1
        }

        withContext(mainDispatcher) {
            state = state.copy(
                selectedSearchOccurrence = updatedSelectedOccurrence,
                selectedResultIndex = selectedResultIndex
            )
        }
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

    internal data class SearchResult(
        internal val searchQuery: String?,
        internal val searchOccurrences: Map<Int, SearchOccurrence>,
        internal val selectedSearchOccurrence: SelectedSearchOccurrence?,
        internal val resultCount: Int,
        internal val selectedResultIndex: Int,
    )
}
