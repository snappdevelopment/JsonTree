package com.sebastianneubauer.jsontree

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
    val rangeIndex: Int,
)

internal class JsonTreeSearch(
    private val defaultDispatcher: CoroutineDispatcher
) {

//    private var searchState = mutableStateOf(SearchState(initialSearchQuery = null))
//    val state: State<SearchState> = searchState

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
            selectedSearchOccurrence = searchOccurrences[0]?.let {
                SelectedSearchOccurrence(
                    occurrence = it,
                    rangeIndex = 0,
                )
            },
        )
    }

//        LaunchedEffect(searchQuery) {
//            jsonSearchResultState.state = SearchResult(
//                searchQuery = searchQuery,
//                totalListSize = null,
//                resultIndices = emptyList(),
//                selectedResultIndex = -1
//            )
//            withContext(defaultDispatcher) {
//                jsonTreeList.forEach {
//                    if (it is JsonTreeElement.Collapsable &&
//                        it.state == TreeState.COLLAPSED &&
//                        it.childContains(searchQuery)
//                    ) {
//                        jsonTreeParser.expandOrCollapseItem(it, true)
//                    }
//                }
//
//                val resultIndices = jsonTreeList
//                    .mapIndexed { index, jsonTreeElement ->
//                        if (jsonTreeElement.contains(searchQuery)) index else null
//                    }.filterNotNull()
//
//                jsonSearchResultState.state = SearchResult(
//                    searchQuery = searchQuery,
//                    totalListSize = jsonTreeList.size,
//                    resultIndices = resultIndices,
//                    selectedResultIndex = -1,
//                )
//            }
//        }
//
//        LaunchedEffect(jsonTreeList.size) {
//            if (jsonSearchResultState.state.totalListSize == jsonTreeList.size) return@LaunchedEffect
//
//            jsonSearchResultState.state = SearchResult(
//                searchQuery = null,
//                totalListSize = null,
//                resultIndices = emptyList(),
//                selectedResultIndex = -1,
//            )
//            withContext(Dispatchers.Default) {
//                val highlightedLines = jsonTreeList.mapIndexed { index, jsonTreeElement ->
//                    if (jsonTreeElement.hasMatch(searchQuery)) index else null
//                }.filterNotNull()
//                jsonSearchResultState.state =
//                    SearchResult(
//                        searchQuery = searchQuery,
//                        totalListSize = jsonTreeList.size,
//                        resultIndices = highlightedLines,
//                        selectedResultIndex = -1,
//                    )
//            }
//        }
//    }

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

    private fun JsonTreeElement.occurrences(searchQuery: String): Int {
        return when (this) {
            is JsonTreeElement.Primitive -> (key?.countOccurrences(searchQuery) ?: 0) + value.content.countOccurrences(searchQuery)
            is JsonTreeElement.Collapsable.Array -> key?.countOccurrences(searchQuery) ?: 0
            is JsonTreeElement.Collapsable.Object -> key?.countOccurrences(searchQuery) ?: 0
            is JsonTreeElement.EndBracket -> 0
        }
    }

    private fun String.countOccurrences(searchQuery: String): Int {
        return split(searchQuery, ignoreCase = true).size - 1
    }

    private fun JsonTreeElement.contains(searchQuery: String): Boolean {
        return when (this) {
            is JsonTreeElement.Primitive -> key?.contains(searchQuery, ignoreCase = true) == true || value.content.contains(searchQuery, ignoreCase = true)
            is JsonTreeElement.Collapsable.Array -> key?.contains(searchQuery, ignoreCase = true) == true
            is JsonTreeElement.Collapsable.Object -> key?.contains(searchQuery, ignoreCase = true) == true
            is JsonTreeElement.EndBracket -> false
        }
    }

    private fun JsonTreeElement.Collapsable.childContains(searchQuery: String): Boolean {
        return children.values.any { it.contains(searchQuery) }
    }
}
