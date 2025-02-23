package com.sebastianneubauer.jsontree.search

import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.search.SearchState.SearchResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal data class SearchOccurrence(
    // the index in the JsonTree list
    val listIndex: Int,
    // the ranges of the search query matches
    val ranges: List<Range>,
) {
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
        // create Regex here for better performance
        val searchRegex = Regex("(?i)${Regex.escape(searchQuery)}")

        val searchOccurrences = buildMap {
            jsonTreeList.forEachIndexed { index, jsonTreeElement ->
                jsonTreeElement
                    .ranges(searchRegex)
                    .takeIf { it.any() } // isNotEmpty
                    ?.let { ranges ->
                        put(
                            key = index,
                            value = SearchOccurrence(
                                listIndex = index,
                                ranges = ranges.toList()
                            )
                        )
                    }
            }
        }

        SearchResult(
            query = searchQuery,
            occurrences = searchOccurrences,
            totalResults = searchOccurrences.values.sumOf { it.ranges.size },
            selectedResultIndex = if (searchOccurrences.isNotEmpty()) 0 else null,
            selectedOccurrence = searchOccurrences.values.firstOrNull()?.let {
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
    private fun JsonTreeElement.ranges(searchRegex: Regex): Sequence<SearchOccurrence.Range> {
        return when (this) {
            is JsonTreeElement.Primitive -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    searchRegex.findRanges(key) + searchRegex.findRanges(value.content, isKey = false)
                } else {
                    searchRegex.findRanges(value.content, isKey = false)
                }
            }
            is JsonTreeElement.Collapsable.Array -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    searchRegex.findRanges(key)
                } else {
                    emptySequence()
                }
            }
            is JsonTreeElement.Collapsable.Object -> {
                if (parentType != JsonTreeElement.ParentType.ARRAY) {
                    searchRegex.findRanges(key)
                } else {
                    emptySequence()
                }
            }
            is JsonTreeElement.EndBracket -> emptySequence()
        }
    }

    private fun Regex.findRanges(input: String?, isKey: Boolean = true): Sequence<SearchOccurrence.Range> {
        if (input == null) return emptySequence()

        return findAll(input).mapNotNull {
            if (it.value.isEmpty()) return@mapNotNull null
            if (isKey) {
                SearchOccurrence.Range.Key(it.range)
            } else {
                SearchOccurrence.Range.Value(it.range)
            }
        }
    }
}
