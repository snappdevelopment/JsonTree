package com.sebastianneubauer.jsontree.search

import com.sebastianneubauer.jsontree.JsonTreeElement
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal data class SearchOccurrence(
    // the index in the JsonTree list
    val listIndex: Int,
    // the ranges of the search query matches
    val ranges: List<Range>,
) {
    // the amount of matches
    val rangeCount: Int
        get() = ranges.size

    sealed interface Range {
        val range: IntRange

        data class Key(
            override val range: IntRange
        ) : Range

        data class Value(
            override val range: IntRange
        ) : Range
    }
}

internal data class SelectedSearchOccurrence(
    val occurrence: SearchOccurrence,
    val range: SearchOccurrence.Range,
)

internal class JsonTreeSearch(
    private val defaultDispatcher: CoroutineDispatcher
) {
    suspend fun search(
        searchQuery: String,
        jsonTreeList: List<JsonTreeElement>,
    ): SearchResult = withContext(defaultDispatcher) {
        val searchOccurrences = buildMap {
            jsonTreeList.forEachIndexed { index, jsonTreeElement ->
                SearchOccurrence(
                    listIndex = index,
                    ranges = jsonTreeElement.ranges(searchQuery)
                )
                    .takeIf { it.rangeCount > 0 }
                    ?.let { put(index, it) }
            }
        }

        SearchResult(
            searchQuery = searchQuery,
            searchOccurrences = searchOccurrences,
            resultCount = searchOccurrences.values.sumOf { it.rangeCount },
            selectedResultIndex = if (searchOccurrences.isNotEmpty()) 0 else -1,
            selectedSearchOccurrence = searchOccurrences.values.firstOrNull()?.let {
                SelectedSearchOccurrence(
                    occurrence = it,
                    range = it.ranges.first(),
                )
            },
        )
    }

    /**
     * Collects ranges of search results, but ignores the key if it is an index of an array,
     * because indices are not always visible.
     */
    private fun JsonTreeElement.ranges(searchQuery: String): List<SearchOccurrence.Range> {
        return when (this) {
            is JsonTreeElement.Primitive -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    (key?.ranges(searchQuery)?.map { SearchOccurrence.Range.Key(it) }.orEmpty()) +
                        value.content.ranges(searchQuery).map { SearchOccurrence.Range.Value(it) }
                } else {
                    value.content.ranges(searchQuery).map { SearchOccurrence.Range.Value(it) }
                }
            }
            is JsonTreeElement.Collapsable.Array -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    key?.ranges(searchQuery)?.map { SearchOccurrence.Range.Key(it) }.orEmpty()
                } else {
                    emptyList()
                }
            }
            is JsonTreeElement.Collapsable.Object -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    key?.ranges(searchQuery)?.map { SearchOccurrence.Range.Key(it) }.orEmpty()
                } else {
                    emptyList()
                }
            }
            is JsonTreeElement.EndBracket -> emptyList()
        }
    }

    private fun String.ranges(searchQuery: String): List<IntRange> {
        return "(?i)${Regex.escape(searchQuery)}"
            .toRegex()
            .findAll(this)
            .map { it.range }
            .toList()
    }
}
