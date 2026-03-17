package com.sebastianneubauer.jsontree.diff

import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastMap
import com.sebastianneubauer.jsontree.JsonTreeElement
import com.sebastianneubauer.jsontree.JsonTreeParser
import com.sebastianneubauer.jsontree.JsonTreeParserState
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.diff.JsonTreeDifferState.JsonDiffElement
import com.sebastianneubauer.jsontree.util.toRenderString
import io.github.petertrr.diffutils.text.DiffLineNormalizer
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import io.github.petertrr.diffutils.text.DiffTagGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.collections.orEmpty
import kotlin.coroutines.resume
import kotlin.time.Clock

internal class JsonTreeDiffer(
    val defaultDispatcher: CoroutineDispatcher,
    val mainDispatcher: CoroutineDispatcher
) {
    val state = MutableStateFlow<JsonTreeDifferState>(JsonTreeDifferState.Loading)

    suspend fun diff(
        original: String,
        revised: String,
        showInlineDiffs: Boolean,
    ) = withContext(defaultDispatcher) {
        if(state.value !is JsonTreeDifferState.Loading) {
            withContext(mainDispatcher) {
                state.value = JsonTreeDifferState.Loading
            }
        }

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

        val originalJsonTreeMapDeferred = runAsync { originalJsonTreeList.groupBy { it.toRenderString() } }
        val revisedJsonTreeMapDeferred = runAsync { revisedJsonTreeList.groupBy { it.toRenderString() } }

        val diffTagGenerator =  object : DiffTagGenerator {
            override fun generateClose(tag: DiffRow.Tag): String = inlineDiffTagClosed
            override fun generateOpen(tag: DiffRow.Tag): String = inlineDiffTagOpen
        }

        val diffRows = DiffRowGenerator(
            columnWidth = 0, // Needs to be 0, otherwise HTML linebreaks will be inserted in diff lines.
            showInlineDiffs = showInlineDiffs,
            lineNormalizer = DiffLineNormalizer { line -> line },
            newTag = diffTagGenerator,
            oldTag = diffTagGenerator,
        ).generateDiffRows(
            originalJsonTreeList.fastMap { it.toRenderString() },
            revisedJsonTreeList.fastMap { it.toRenderString() }
        )

        val originalJsonTreeMap = originalJsonTreeMapDeferred.await()
        val revisedJsonTreeMap = revisedJsonTreeMapDeferred.await()

        val usedOriginalIds = mutableMapOf<String, MutableList<String>>()
        val usedRevisedIds = mutableMapOf<String, MutableList<String>>()

        val diffElements = diffRows.fastMap { diffRow ->
            val (oldLineDiffIndices, newLineDiffIndices) = if(diffRow.tag == DiffRow.Tag.CHANGE) {
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

            val originalDiffElementDeferred = runAsync {
                getOriginalDiffElement(
                    diffRow = strippedTagDiffRow,
                    usedIds = usedOriginalIds,
                    originalJsonTreeMap = originalJsonTreeMap,
                    inlineDiffsIndices = oldLineDiffIndices
                )
            }

            val revisedDiffElementDeferred = runAsync {
                getRevisedDiffElement(
                    diffRow = strippedTagDiffRow,
                    usedIds = usedRevisedIds,
                    revisedJsonTreeMap = revisedJsonTreeMap,
                    inlineDiffsIndices = newLineDiffIndices
                )
            }

            Pair(originalDiffElementDeferred.await(), revisedDiffElementDeferred.await())
        }

        withContext(mainDispatcher) {
            state.value = JsonTreeDifferState.Ready(
                diffElements = diffElements
            )
        }
    }

    private fun getOriginalDiffElement(
        diffRow: DiffRow,
        usedIds: MutableMap<String, MutableList<String>>,
        originalJsonTreeMap: Map<String, List<JsonTreeElement>>,
        inlineDiffsIndices: List<Pair<Int, Int>>
    ): JsonDiffElement {
        fun findJsonTreeElement(): JsonTreeElement {
            val jsonTreeElements = originalJsonTreeMap.getValue(diffRow.oldLine)
            return jsonTreeElements.fastFirst { it.id !in usedIds[diffRow.oldLine].orEmpty() }
        }

        fun addToUsedIds(id: String) {
            val currentIds = usedIds[diffRow.oldLine] ?: mutableListOf()
            usedIds[diffRow.oldLine] = currentIds.apply { add(id) }
        }

        return when(diffRow.tag) {
            DiffRow.Tag.EQUAL -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Equal(jsonTreeElement)
            }
            DiffRow.Tag.CHANGE -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Change(
                    jsonTreeElement = jsonTreeElement,
                    inlineDiffIndices = inlineDiffsIndices
                )
            }
            DiffRow.Tag.INSERT -> {
                JsonDiffElement.Insertion(null)
            }
            DiffRow.Tag.DELETE -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Deletion(jsonTreeElement)
            }
        }
    }

    private fun getRevisedDiffElement(
        diffRow: DiffRow,
        usedIds: MutableMap<String, MutableList<String>>,
        revisedJsonTreeMap: Map<String, List<JsonTreeElement>>,
        inlineDiffsIndices: List<Pair<Int,Int>>
    ): JsonDiffElement {
        fun findJsonTreeElement(): JsonTreeElement {
            val jsonTreeElements = revisedJsonTreeMap.getValue(diffRow.newLine)
            return jsonTreeElements.fastFirst { it.id !in usedIds[diffRow.newLine].orEmpty() }
        }

        fun addToUsedIds(id: String) {
            val currentIds = usedIds[diffRow.newLine] ?: mutableListOf()
            usedIds[diffRow.newLine] = currentIds.apply { add(id) }
        }

        return when(diffRow.tag) {
            DiffRow.Tag.EQUAL -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Equal(jsonTreeElement)
            }
            DiffRow.Tag.CHANGE -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Change(
                    jsonTreeElement = jsonTreeElement,
                    inlineDiffIndices = inlineDiffsIndices
                )
            }
            DiffRow.Tag.INSERT -> {
                val jsonTreeElement = findJsonTreeElement()
                addToUsedIds(id = jsonTreeElement.id)
                JsonDiffElement.Insertion(jsonTreeElement)
            }
            DiffRow.Tag.DELETE -> JsonDiffElement.Deletion(null)
        }
    }

    private suspend fun getJsonTreeList(json: String): JsonTreeParserState {
        val originalParser = JsonTreeParser(
            json = json,
            defaultDispatcher = defaultDispatcher,
            mainDispatcher = mainDispatcher
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

    private fun <T> CoroutineScope.runAsync(block: () -> T): Deferred<T> = async {
        suspendCancellableCoroutine { continuation ->
            val result = block()
            continuation.resume(result)
        }
    }

    private val inlineDiffTagOpen = "JSON_TREE_DIFF_START_TAG"
    private val inlineDiffTagClosed = "JSON_TREE_DIFF_END_TAG"
}

