package com.sebastianneubauer.jsontree.diff

import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeParser
import com.sebastianneubauer.jsontree.JsonTreeParserState
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.util.toRenderString
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

internal class JsonTreeDiffer(
    val defaultDispatcher: CoroutineDispatcher,
    val mainDispatcher: CoroutineDispatcher
) {

    val state = MutableStateFlow<JsonTreeDifferState>(JsonTreeDifferState.Loading)

    suspend fun diff(
        original: String,
        revised: String,
    ) = withContext(defaultDispatcher) {
        val originalJsonTreeListDeferred = async { getJsonTreeList(original) }
        val revisedJsonTreeListDeferred = async { getJsonTreeList(revised) }

        val originalJsonTreeListResult = originalJsonTreeListDeferred.await()
        val revisedJsonTreeListResult = revisedJsonTreeListDeferred.await()

        val (originalJsonTreeList, revisedJsonTreeList) = when {
            originalJsonTreeListResult is JsonTreeParserState.Parsing.Error -> {
                withContext(mainDispatcher) {
                    state.value = JsonTreeDifferState.Error.OriginalJsonError(originalJsonTreeListResult.throwable)
                }
                return@withContext
            }
            revisedJsonTreeListResult is JsonTreeParserState.Parsing.Error -> {
                withContext(mainDispatcher) {
                    state.value = JsonTreeDifferState.Error.RevisedJsonError(revisedJsonTreeListResult.throwable)
                }
                return@withContext
            }
            else -> {
                (originalJsonTreeListResult as JsonTreeParserState.Ready).list to (revisedJsonTreeListResult as JsonTreeParserState.Ready).list
            }
        }

        val diffRowGenerator =  object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String = inlineDiffTagClosed
            override fun generateOpen(tag: DiffRow.Tag): String = inlineDiffTagOpen
        }

        val diffRows = DiffRowGenerator(
            showInlineDiffs = true,
            newTag = diffRowGenerator,
            oldTag = diffRowGenerator,
        ).generateDiffRows(
            originalJsonTreeList.map { it.toRenderString() },
            revisedJsonTreeList.map { it.toRenderString() }
        )

        val inlineDiffsIndices = diffRows.map { diffRow ->
            if(diffRow.tag == DiffRow.Tag.CHANGE) {
                val oldLineIndices = diffRow.oldLine.findInlineDiffTagIndices()
                val newLineIndices = diffRow.newLine.findInlineDiffTagIndices()
                Pair(oldLineIndices, newLineIndices)
            } else {
                Pair(emptyList(), emptyList())
            }
        }

        val strippedTagDiffRows = diffRows.map { diffRow ->
            diffRow.copy(
                oldLine = diffRow.oldLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, ""),
                newLine = diffRow.newLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, "")
            )
        }

        val originalDiffElements = getOriginalDiffElements(
            strippedTagDiffRows = strippedTagDiffRows,
            originalJsonTreeList = originalJsonTreeList,
            inlineDiffsIndices = inlineDiffsIndices
        )

        val revisedDiffElements = getRevisedDiffElements(
            strippedTagDiffRows = strippedTagDiffRows,
            revisedJsonTreeList = revisedJsonTreeList,
            inlineDiffsIndices = inlineDiffsIndices
        )

        withContext(mainDispatcher) {
            state.value = JsonTreeDifferState.Ready(
                originalJsonDiffElements = originalDiffElements,
                revisedJsonDiffElements = revisedDiffElements,
            )
        }
    }

    private fun getOriginalDiffElements(
        strippedTagDiffRows: List<DiffRow>,
        originalJsonTreeList: List<JsonTreeElement>,
        inlineDiffsIndices: List<Pair<List<Pair<Int, Int>>, List<Pair<Int,Int>>>>
    ): List<JsonDiffElement> {
        val usedIds = mutableListOf<String>()

        fun findJsonTreeElement(diffLine: String): JsonTreeElement {
            return originalJsonTreeList.first { jsonTreeElement ->
                diffLine == jsonTreeElement.toRenderString() && jsonTreeElement.id !in usedIds
            }
        }

        return strippedTagDiffRows.mapIndexed { index, diffRow ->
            when(diffRow.tag) {
                DiffRow.Tag.EQUAL -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.oldLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Equal(jsonTreeElement)
                }
                DiffRow.Tag.CHANGE -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.oldLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Change(
                        jsonTreeElement = jsonTreeElement,
                        inlineDiffIndices = inlineDiffsIndices[index].first
                    )
                }
                DiffRow.Tag.INSERT -> {
                    JsonDiffElement.Insertion(null)
                }
                DiffRow.Tag.DELETE -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.oldLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Deletion(jsonTreeElement)
                }
            }
        }
    }

    private fun getRevisedDiffElements(
        strippedTagDiffRows: List<DiffRow>,
        revisedJsonTreeList: List<JsonTreeElement>,
        inlineDiffsIndices: List<Pair<List<Pair<Int, Int>>, List<Pair<Int,Int>>>>
    ): List<JsonDiffElement> {
        val usedIds = mutableListOf<String>()

        fun findJsonTreeElement(diffLine: String): JsonTreeElement {
            return revisedJsonTreeList.first { jsonTreeElement ->
                diffLine == jsonTreeElement.toRenderString() && jsonTreeElement.id !in usedIds
            }
        }

        return strippedTagDiffRows.mapIndexed { index, diffRow ->
            when(diffRow.tag) {
                DiffRow.Tag.EQUAL -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.newLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Equal(jsonTreeElement)
                }
                DiffRow.Tag.CHANGE -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.newLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Change(
                        jsonTreeElement = jsonTreeElement,
                        inlineDiffIndices = inlineDiffsIndices[index].second
                    )
                }
                DiffRow.Tag.INSERT -> {
                    val jsonTreeElement = findJsonTreeElement(diffRow.newLine)
                    usedIds.add(jsonTreeElement.id)
                    JsonDiffElement.Insertion(jsonTreeElement)
                }
                DiffRow.Tag.DELETE -> JsonDiffElement.Deletion(null)
            }
        }
    }

    private suspend fun getJsonTreeList(json: String): JsonTreeParserState {
        val originalParser = JsonTreeParser(
            json = json,
            defaultDispatcher = Dispatchers.Default,
            mainDispatcher = Dispatchers.Main
        ).also { it.init(TreeState.EXPANDED) }

        return when(val state = originalParser.state.value) {
            is JsonTreeParserState.Ready -> state
            is JsonTreeParserState.Parsing.Error -> state
            is JsonTreeParserState.Loading,
            is JsonTreeParserState.Parsing.Parsed -> error("Impossible state $state!")
        }
    }

    private fun String.findInlineDiffTagIndices(): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var strippedIndex = 0
        var currentIndex = 0

        while (currentIndex < length) {
            val openTagIndex = indexOf(inlineDiffTagOpen, currentIndex)
            if (openTagIndex == -1) break

            // Add non-bold text length to stripped index
            strippedIndex += (openTagIndex - currentIndex)

            val contentStart = openTagIndex + inlineDiffTagOpen.length
            val closeTagIndex = indexOf(inlineDiffTagClosed, contentStart)

            if (closeTagIndex == -1) break

            val contentLength = closeTagIndex - contentStart
            result.add(strippedIndex to (strippedIndex + contentLength))

            strippedIndex += contentLength
            currentIndex = closeTagIndex + inlineDiffTagClosed.length
        }

        return result
    }

    private val inlineDiffTagOpen = "<$$$$>"
    private val inlineDiffTagClosed = "</$$$$>"
}

