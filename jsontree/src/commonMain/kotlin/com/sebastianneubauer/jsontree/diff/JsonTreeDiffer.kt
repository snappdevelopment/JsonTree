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
            columnWidth = 0, // Needs to be 0, otherwise HTML linebreaks will be inserted in diff lines.
            showInlineDiffs = true,
            newTag = diffRowGenerator,
            oldTag = diffRowGenerator,
        ).generateDiffRows(
            originalJsonTreeList.map { it.toRenderString() },
            revisedJsonTreeList.map { it.toRenderString() }
        )

        val usedOriginalIds = mutableListOf<String>()
        val usedRevisedIds = mutableListOf<String>()

        val diffElements = diffRows.map { diffRow ->
            println("DiffRow: ${diffRow.tag}: ${diffRow.oldLine} -> ${diffRow.newLine}")

            val inlineDiffsIndices = if(diffRow.tag == DiffRow.Tag.CHANGE) {
                val oldLineIndices = diffRow.oldLine.findInlineDiffTagIndices()
                val newLineIndices = diffRow.newLine.findInlineDiffTagIndices()
                Pair(oldLineIndices, newLineIndices)
            } else {
                Pair(emptyList(), emptyList())
            }

            val strippedTagDiffRow = diffRow.copy(
                oldLine = diffRow.oldLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, ""),
                newLine = diffRow.newLine.replace(inlineDiffTagOpen, "").replace(inlineDiffTagClosed, "")
            )
            println("Stripped: ${strippedTagDiffRow.tag}: ${strippedTagDiffRow.oldLine} -> ${strippedTagDiffRow.newLine}")

            val originalDiffElement = getOriginalDiffElement(
                diffRow = strippedTagDiffRow,
                usedIds = usedOriginalIds,
                originalJsonTreeList = originalJsonTreeList,
                inlineDiffsIndices = inlineDiffsIndices.first
            )

            val revisedDiffElement = getRevisedDiffElement(
                diffRow = strippedTagDiffRow,
                usedIds = usedRevisedIds,
                revisedJsonTreeList = revisedJsonTreeList,
                inlineDiffsIndices = inlineDiffsIndices.second
            )

            Pair(originalDiffElement, revisedDiffElement)
        }

        withContext(mainDispatcher) {
            state.value = JsonTreeDifferState.Ready(
                diffElements = diffElements
            )
        }
    }

    private fun getOriginalDiffElement(
        diffRow: DiffRow,
        usedIds: MutableList<String>,
        originalJsonTreeList: List<JsonTreeElement>,
        inlineDiffsIndices: List<Pair<Int, Int>>
    ): JsonDiffElement {
        fun findJsonTreeElement(diffLine: String): JsonTreeElement {
            return originalJsonTreeList.first { jsonTreeElement ->
                diffLine == jsonTreeElement.toRenderString() && jsonTreeElement.id !in usedIds
            }
        }

        return when(diffRow.tag) {
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
                    inlineDiffIndices = inlineDiffsIndices
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

    private fun getRevisedDiffElement(
        diffRow: DiffRow,
        usedIds: MutableList<String>,
        revisedJsonTreeList: List<JsonTreeElement>,
        inlineDiffsIndices: List<Pair<Int,Int>>
    ): JsonDiffElement {
        fun findJsonTreeElement(diffLine: String): JsonTreeElement {
            return revisedJsonTreeList.first { jsonTreeElement ->
                diffLine == jsonTreeElement.toRenderString() && jsonTreeElement.id !in usedIds
            }
        }

        return when(diffRow.tag) {
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
                    inlineDiffIndices = inlineDiffsIndices
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

    private val inlineDiffTagOpen = "JSON_TREE_DIFF_START_TAG"
    private val inlineDiffTagClosed = "JSON_TREE_DIFF_END_TAG"
}

