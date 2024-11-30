package com.sebastianneubauer.jsontree

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

public class SearchStateTest {

    @Test
    public fun `the initial state is has no results`(): TestResult = runTest {
        val searchState = SearchState()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `updating the query in the state should update the query variable`(): TestResult = runTest {
        val searchState = SearchState()
        assertEquals(actual = searchState.searchQuery, expected = null)

        searchState.state = searchState.state.copy(searchQuery = "test")
        assertEquals(actual = searchState.searchQuery, expected = "test")
    }

    @Test
    public fun `updating the resultCount in the state should update the resultCount variable`(): TestResult = runTest {
        val searchState = SearchState()
        assertEquals(actual = searchState.resultCount, expected = 0)

        searchState.state = searchState.state.copy(resultCount = 1)
        assertEquals(actual = searchState.resultCount, expected = 1)
    }

    @Test
    public fun `updating the selectedResultIndex in the state should update the selectedResult variable`(): TestResult = runTest {
        val searchState = SearchState()
        assertEquals(actual = searchState.selectedResult, expected = 0)

        searchState.state = searchState.state.copy(selectedResultIndex = 0)
        assertEquals(actual = searchState.selectedResult, expected = 1)
    }

    @Test
    public fun `calling reset() should reset the state`(): TestResult = runTest {
        val searchState = SearchState()
        val searchResult = SearchResult(
            searchQuery = "test",
            searchOccurrences = mapOf(0 to SearchOccurrence(0, emptyList())),
            selectedSearchOccurrence = SelectedSearchOccurrence(
                occurrence = SearchOccurrence(0, emptyList()),
                range = SearchOccurrence.Range.Key(IntRange(0,1))),
            resultCount = 1,
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
        val searchState = SearchState()
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectNext()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `calling selectNext() and current range is not the last one, selects the next range`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences

        searchState.selectNext()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange2,
                ),
                selectedResultIndex = 1
            )
        )
    }

    @Test
    public fun `calling selectNext() and current range is the last one, selects the next occurrence`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences.copy(
            selectedSearchOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence,
                range = searchRange2,
            ),
            selectedResultIndex = 1
        )

        searchState.selectNext()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence2,
                    range = searchRange3,
                ),
                selectedResultIndex = 2
            )
        )
    }

    @Test
    public fun `calling selectNext() and current occurrence is the last one, selects the first occurrence`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences.copy(
            selectedSearchOccurrence = SelectedSearchOccurrence(
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
        val searchState = SearchState()
        assertEquals(actual = searchState.state, expected = initialSearchResult)

        searchState.selectPrevious()
        assertEquals(actual = searchState.state, expected = initialSearchResult)
    }

    @Test
    public fun `calling selectPrevious() and current range is not the first one, selects the previous range`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences.copy(
            selectedSearchOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence,
                range = searchRange2,
            ),
            selectedResultIndex = 1
        )

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange,
                ),
                selectedResultIndex = 0
            )
        )
    }

    @Test
    public fun `calling selectPrevious() and current range is the first one, selects the previous occurrence`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences.copy(
            selectedSearchOccurrence = SelectedSearchOccurrence(
                occurrence = searchOccurrence2,
                range = searchRange3,
            ),
            selectedResultIndex = 2
        )

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence,
                    range = searchRange2,
                ),
                selectedResultIndex = 1
            )
        )
    }

    @Test
    public fun `calling selectPrevious() and current occurrence is the first one, selects the last occurrence`(): TestResult = runTest {
        val searchState = SearchState()
        searchState.state = resultWithOccurrences

        searchState.selectPrevious()
        assertEquals(
            actual = searchState.state,
            expected = resultWithOccurrences.copy(
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = searchOccurrence2,
                    range = searchRange3,
                ),
                selectedResultIndex = 2
            )
        )
    }

    private val initialSearchResult = SearchResult(
        searchQuery = null,
        searchOccurrences = emptyMap(),
        selectedSearchOccurrence = null,
        selectedResultIndex = -1,
        resultCount = 0
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
        searchQuery = "t",
        searchOccurrences = mapOf(0 to searchOccurrence, 1 to searchOccurrence2),
        selectedSearchOccurrence = SelectedSearchOccurrence(
            occurrence = searchOccurrence,
            range = searchRange,
        ),
        resultCount = 3,
        selectedResultIndex = 0
    )

    private val resultWithoutSelectedOccurrence = SearchResult(
        searchQuery = "t",
        searchOccurrences = mapOf(0 to searchOccurrence, 1 to searchOccurrence2),
        selectedSearchOccurrence = null,
        resultCount = 1,
        selectedResultIndex = -1
    )
}