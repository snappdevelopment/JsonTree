package com.sebastianneubauer.jsontree

import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Array
import com.sebastianneubauer.jsontree.JsonTreeElement.Collapsable.Object
import com.sebastianneubauer.jsontree.search.JsonTreeSearch
import com.sebastianneubauer.jsontree.search.SearchOccurrence
import com.sebastianneubauer.jsontree.search.SearchResult
import com.sebastianneubauer.jsontree.search.SelectedSearchOccurrence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

public class JsonTreeSearchTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val underTest = JsonTreeSearch(
        defaultDispatcher = UnconfinedTestDispatcher()
    )

    @Test
    public fun `query with one occurrence in key should return result with one occurrence in key`(): TestResult = runTest {
        val result = underTest.search(searchQuery = "array1", jsonTreeList = TestData.expandedList)

        val range = SearchOccurrence.Range.Key(range = IntRange(0, 5))
        val occurrence = SearchOccurrence(
            listIndex = 1,
            ranges = listOf(range)
        )
        assertEquals(
            actual = result,
            expected = SearchResult(
                searchQuery = "array1",
                searchOccurrences = mapOf(1 to occurrence),
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = occurrence,
                    range = range
                ),
                selectedResultIndex = 0,
                resultCount = 1
            )
        )
    }

    @Test
    public fun `query with multiple occurrences in key should return result with multiple occurrence in key`(): TestResult = runTest {
        val result = underTest.search(searchQuery = "i", jsonTreeList = TestData.expandedList)

        val range1 = SearchOccurrence.Range.Key(range = IntRange(2, 2))
        val range2 = SearchOccurrence.Range.Key(range = IntRange(4, 4))
        val range3 = SearchOccurrence.Range.Key(range = IntRange(6, 6))
        val occurrence = SearchOccurrence(
            listIndex = 4,
            ranges = listOf(range1, range2, range3)
        )
        assertEquals(
            actual = result,
            expected = SearchResult(
                searchQuery = "i",
                searchOccurrences = mapOf(4 to occurrence),
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = occurrence,
                    range = range1
                ),
                selectedResultIndex = 0,
                resultCount = 3
            )
        )
    }

    @Test
    public fun `query with one occurrence in value should return result with one occurrence in value`(): TestResult = runTest {
        val result = underTest.search(searchQuery = "value1", jsonTreeList = TestData.expandedList)

        val range = SearchOccurrence.Range.Value(range = IntRange(0, 5))
        val occurrence = SearchOccurrence(
            listIndex = 2,
            ranges = listOf(range)
        )
        assertEquals(
            actual = result,
            expected = SearchResult(
                searchQuery = "value1",
                searchOccurrences = mapOf(2 to occurrence),
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = occurrence,
                    range = range
                ),
                selectedResultIndex = 0,
                resultCount = 1
            )
        )
    }

    @Test
    public fun `query with multiple occurrences in value should return result with multiple occurrence in value`(): TestResult = runTest {
        val result = underTest.search(searchQuery = "second", jsonTreeList = TestData.expandedList)

        val range1 = SearchOccurrence.Range.Value(range = IntRange(0, 5))
        val range2 = SearchOccurrence.Range.Value(range = IntRange(6, 11))
        val occurrence = SearchOccurrence(
            listIndex = 4,
            ranges = listOf(range1, range2)
        )
        assertEquals(
            actual = result,
            expected = SearchResult(
                searchQuery = "second",
                searchOccurrences = mapOf(4 to occurrence),
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = occurrence,
                    range = range1
                ),
                selectedResultIndex = 0,
                resultCount = 2
            )
        )
    }

    @Test
    public fun `query with occurrences in key and value should return result with occurrences in key and value`(): TestResult = runTest {
        val result = underTest.search(searchQuery = "r", jsonTreeList = TestData.expandedList)

        val range1 = SearchOccurrence.Range.Key(range = IntRange(1, 1))
        val range2 = SearchOccurrence.Range.Key(range = IntRange(2, 2))
        val occurrence = SearchOccurrence(
            listIndex = 1,
            ranges = listOf(range1, range2)
        )
        val occurrence2 = SearchOccurrence(
            listIndex = 4,
            ranges = listOf(range1)
        )
        assertEquals(
            actual = result,
            expected = SearchResult(
                searchQuery = "r",
                searchOccurrences = mapOf(1 to occurrence, 4 to occurrence2),
                selectedSearchOccurrence = SelectedSearchOccurrence(
                    occurrence = occurrence,
                    range = range1
                ),
                selectedResultIndex = 0,
                resultCount = 3
            )
        )
    }

    private object TestData {
        val primitive1 = JsonTreeElement.Primitive(
            id = "primitive1",
            level = 2,
            isLastItem = false,
            key = "0",
            value = JsonPrimitive("value1"),
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val primitive2 = JsonTreeElement.Primitive(
            id = "primitive2",
            level = 3,
            isLastItem = true,
            key = "primitive2",
            value = JsonPrimitive("secondSecond"),
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val object1 = Object(
            id = "object1",
            level = 2,
            state = TreeState.EXPANDED,
            children = mapOf(
                "primitive2" to primitive2
            ),
            isLastItem = true,
            key = "1",
            parentType = JsonTreeElement.ParentType.ARRAY
        )

        val array1 = Array(
            id = "array1",
            level = 1,
            state = TreeState.EXPANDED,
            children = mapOf(
                "0" to primitive1,
                "1" to object1
            ),
            isLastItem = true,
            key = "array1",
            parentType = JsonTreeElement.ParentType.OBJECT
        )

        val rootObject = Object(
            id = "rootObject",
            level = 0,
            state = TreeState.EXPANDED,
            children = mapOf("array1" to array1),
            isLastItem = true,
            key = null,
            parentType = JsonTreeElement.ParentType.NONE
        )

        val expandedList = listOf(
            rootObject,
            array1,
            primitive1,
            object1,
            primitive2,
            object1.endBracket,
            array1.endBracket,
            rootObject.endBracket
        )
    }
}