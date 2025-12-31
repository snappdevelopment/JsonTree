package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.search.SearchOccurrence
import com.sebastianneubauer.jsontree.search.SearchState
import com.sebastianneubauer.jsontree.search.SearchState.SearchResult
import com.sebastianneubauer.jsontree.search.SelectedSearchOccurrence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

public class SearchStateTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    public fun the_initial_state_is_has_no_results(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun updating_the_query_in_the_state_should_update_the_query_variable(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.query, expected = null)

        searchState.state = searchState.state.copy(query = "test")
        assertEquals(actual = searchState.query, expected = "test")
    }

    @Test
    public fun updating_the_resultCount_in_the_state_should_update_the_resultCount_variable(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.totalResults, expected = 0)

        searchState.state = searchState.state.copy(totalResults = 1)
        assertEquals(actual = searchState.totalResults, expected = 1)
    }

    @Test
    public fun updating_the_selectedResultIndex_in_the_state_should_update_the_selectedResult_variable(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.selectedResultIndex, expected = null)

        searchState.state = searchState.state.copy(selectedResultIndex = 0)
        assertEquals(actual = searchState.selectedResultIndex, expected = 0)
    }

    @Test
    public fun calling_reset_should_reset_the_state(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        val searchResult = SearchResult(
            query = "test",
            occurrences = mapOf(0 to SearchOccurrence(0, emptyList())),
            selectedOccurrence = SelectedSearchOccurrence(
                occurrence = SearchOccurrence(0, emptyList()),
                range = SearchOccurrence.Range.Key(IntRange(0, 1))
            ),
            totalResults = 1,
            selectedResultIndex = 0
        )
        searchState.state = searchResult

        assertEquals(actual = searchState.state, expected = searchResult)

        searchState.reset()
        assertEquals(
            actual = searchState.state,
            expected = initialSearchResult
        )
    }

    @Test
    public fun calling_selectNext_without_results_does_nothing(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectNext()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun calling_selectNext_and_current_range_is_not_the_last_one_selects_the_next_range(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences

        searchState.selectNext()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange2,
                ),
                selectedResultIndex = 1
            )
        )
    }

    @Test
    public fun calling_selectNext_and_current_range_is_the_last_one_selects_the_next_occurrence(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences.copy(
            selectedOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence,
                range = searchRange2,
            ),
            selectedResultIndex = 1
        )

        searchState.selectNext()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence2,
                    range = searchRange3,
                ),
                selectedResultIndex = 2
            )
        )
    }

    @Test
    public fun calling_selectNext_and_current_occurrence_is_the_last_one_selects_the_first_occurrence(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences.copy(
            selectedOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence2,
                range = searchRange3,
            ),
            selectedResultIndex = 2
        )

        searchState.selectNext()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences
        )
    }

    @Test
    public fun calling_selectPrevious_without_results_does_nothing(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectPrevious()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun calling_selectPrevious_and_current_range_is_not_the_first_one_selects_the_previous_range(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences.copy(
            selectedOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence,
                range = searchRange2,
            ),
            selectedResultIndex = 1
        )

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange,
                ),
                selectedResultIndex = 0
            )
        )
    }

    @Test
    public fun calling_selectPrevious_and_current_range_is_the_first_one_selects_the_previous_occurrence(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences.copy(
            selectedOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence2,
                range = searchRange3,
            ),
            selectedResultIndex = 2
        )

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange2,
                ),
                selectedResultIndex = 1
            )
        )
    }

    @Test
    public fun calling_selectPrevious_and_current_occurrence_is_the_first_one_selects_the_last_occurrence(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        searchState.state = resultWithOccurrences

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence2,
                    range = searchRange3,
                ),
                selectedResultIndex = 2
            )
        )
    }

    private val initialSearchResult = SearchResult(
        query = null,
        occurrences = emptyMap(),
        selectedOccurrence = null,
        selectedResultIndex = null,
        totalResults = 0
    )

    private val searchRange = SearchOccurrence.Range.Key(range = IntRange(0, 1))
    private val searchRange2 = SearchOccurrence.Range.Value(range = IntRange(0, 1))
    private val searchOccurrence = SearchOccurrence(
        listIndex = 0,
        ranges = listOf(searchRange, searchRange2)
    )
    private val searchRange3 = SearchOccurrence.Range.Key(range = IntRange(0, 1))
    private val searchOccurrence2 = SearchOccurrence(
        listIndex = 1,
        ranges = listOf(searchRange3)
    )

    private val resultWithOccurrences = SearchResult(
        query = "t",
        occurrences = mapOf(0 to searchOccurrence, 1 to searchOccurrence2),
        selectedOccurrence = SelectedSearchOccurrence(
            occurrence = searchOccurrence,
            range = searchRange,
        ),
        totalResults = 3,
        selectedResultIndex = 0
    )
}
