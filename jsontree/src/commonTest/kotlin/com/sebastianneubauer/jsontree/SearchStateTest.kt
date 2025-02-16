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
    public fun `the initial state is has no results`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `updating the query in the state should update the query variable`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.query, expected = null)

        searchState.state = searchState.state.copy(query = "test")
        assertEquals(actual = searchState.query, expected = "test")
    }

    @Test
    public fun `updating the resultCount in the state should update the resultCount variable`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.totalResults, expected = 0)

        searchState.state = searchState.state.copy(totalResults = 1)
        assertEquals(actual = searchState.totalResults, expected = 1)
    }

    @Test
    public fun `updating the selectedResultIndex in the state should update the selectedResult variable`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.selectedResultIndex, expected = null)

        searchState.state = searchState.state.copy(selectedResultIndex = 0)
        assertEquals(actual = searchState.selectedResultIndex, expected = 0)
    }

    @Test
    public fun `calling reset() should reset the state`(): TestResult = runTest {
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
    public fun `calling selectNext() without results does nothing`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectNext()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `calling selectNext() and current range is not the last one, selects the next range`(): TestResult = runTest {
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
    public fun `calling selectNext() and current range is the last one, selects the next occurrence`(): TestResult = runTest {
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
    public fun `calling selectNext() and current occurrence is the last one, selects the first occurrence`(): TestResult = runTest {
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
    public fun `calling selectPrevious() without results does nothing`(): TestResult = runTest {
        val searchState = SearchState(defaultDispatcher = dispatcher, mainDispatcher = dispatcher)
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectPrevious()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `calling selectPrevious() and current range is not the first one, selects the previous range`(): TestResult = runTest {
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
    public fun `calling selectPrevious() and current range is the first one, selects the previous occurrence`(): TestResult = runTest {
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
    public fun `calling selectPrevious() and current occurrence is the first one, selects the last occurrence`(): TestResult = runTest {
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
